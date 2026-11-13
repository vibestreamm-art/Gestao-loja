package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.components.ConfirmDeleteDialog
import com.example.components.CustomPeriodDialog
import com.example.components.EmptyStateView
import com.example.components.KpiCard
import com.example.components.LuxuryCard
import com.example.components.PeriodFilterRow
import com.example.components.ResultBanner
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.PeriodFilter
import com.example.domain.UiResult
import com.example.entities.ExpenseEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import com.example.viewmodel.FinanceUiState

@Composable
fun FinanceScreen(
    state: FinanceUiState,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onSelectPeriod: (PeriodFilter) -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit,
    onSaveExpense: (id: Long?, desc: String, cat: String, amountCents: Long, dateMillis: Long, notes: String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var isCreatingExpense by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val summary = state.summary

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("finance_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Financeiro",
                subtitle = if (state.period == PeriodFilter.PERSONALIZADO) {
                    "${DateUtils.formatDate(state.customStartMillis)} - ${DateUtils.formatDate(state.customEndMillis)}"
                } else {
                    state.period.label
                },
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingExpense = true; editingExpense = null },
                containerColor = GoldPrimary,
                contentColor = OnGold,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar despesa")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ResultBanner(result = actionResult, onDismiss = onDismissResult) }

            item {
                PeriodFilterRow(
                    selectedPeriod = state.period,
                    onSelectPeriod = onSelectPeriod,
                    onOpenCustomRange = { showCustomDialog = true }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Faturamento (Vendas)",
                        value = CurrencyUtils.formatCents(summary.salesRevenueCents),
                        subtitle = "${summary.salesCount} venda(s) concluída(s)",
                        icon = Icons.Default.ArrowUpward,
                        accentColor = GoldPrimary,
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Lucro Bruto Estimado",
                        value = CurrencyUtils.formatCents(summary.grossProfitCents),
                        subtitle = "Faturamento - CMV",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Despesas",
                        value = CurrencyUtils.formatCents(summary.expensesCents),
                        subtitle = "${summary.expensesCount} lançamento(s)",
                        icon = Icons.Default.ArrowDownward,
                        accentColor = CoralDanger,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Saldo Registrado",
                        value = CurrencyUtils.formatCents(summary.netBalanceCents),
                        subtitle = "Entradas - Despesas",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = if (summary.netBalanceCents >= 0) EmeraldSuccess else CoralDanger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                LuxuryCard(highlightGold = true) {
                    Text("Demonstrativo Transparente", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                    Spacer(modifier = Modifier.height(10.dp))
                    FinanceBreakdownLine("Faturamento bruto de vendas", summary.salesRevenueCents, GoldLight)
                    FinanceBreakdownLine("(-) Custo dos produtos vendidos (CMV)", -summary.cogsCents, AmberWarning)
                    HorizontalDivider(color = SubtleBorder, modifier = Modifier.padding(vertical = 6.dp))
                    FinanceBreakdownLine("(=) Lucro bruto estimado das vendas", summary.grossProfitCents, EmeraldSuccess, bold = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    FinanceBreakdownLine("(+) Sinais/recebimentos de pedidos", summary.orderAdvancesCents, WhiteText)
                    FinanceBreakdownLine("(=) Total de entradas registradas", summary.totalInflowsCents, GoldPrimary)
                    FinanceBreakdownLine("(-) Despesas operacionais", -summary.expensesCents, CoralDanger)
                    HorizontalDivider(color = SubtleBorder, modifier = Modifier.padding(vertical = 6.dp))
                    FinanceBreakdownLine(
                        "(=) Saldo financeiro no período",
                        summary.netBalanceCents,
                        if (summary.netBalanceCents >= 0) EmeraldSuccess else CoralDanger,
                        bold = true
                    )
                }
            }

            item {
                Text("Despesas no Período", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }

            if (state.expenses.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Nenhuma despesa no período",
                        description = "Toque no botão + para lançar aluguel, compras, embalagens, taxas ou outras despesas.",
                        actionLabel = "Cadastrar despesa",
                        onAction = { isCreatingExpense = true; editingExpense = null }
                    )
                }
            } else {
                items(state.expenses, key = { it.id }) { exp ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.description, style = MaterialTheme.typography.titleSmall, color = WhiteText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge(exp.category, CoralDanger, CoralContainer)
                                    Text(DateUtils.formatDate(exp.dateMillis), style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                                }
                                if (exp.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(exp.notes, style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "-${CurrencyUtils.formatCents(exp.amountCents)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CoralDanger,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    IconButton(onClick = { editingExpense = exp; isCreatingExpense = false }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar despesa", tint = GoldPrimary)
                                    }
                                    IconButton(onClick = { expenseToDelete = exp }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir despesa", tint = CoralDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showCustomDialog) {
        CustomPeriodDialog(
            initialStartMillis = state.customStartMillis,
            initialEndMillis = state.customEndMillis,
            onConfirm = { start, end ->
                onSelectCustomRange(start, end)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }

    if (isCreatingExpense || editingExpense != null) {
        ExpenseFormDialog(
            existing = editingExpense,
            onSave = { desc, cat, amountCents, dateMillis, notes ->
                onSaveExpense(editingExpense?.id, desc, cat, amountCents, dateMillis, notes)
                isCreatingExpense = false
                editingExpense = null
            },
            onDismiss = { isCreatingExpense = false; editingExpense = null }
        )
    }

    expenseToDelete?.let { exp ->
        ConfirmDeleteDialog(
            title = "Excluir Despesa",
            message = "Deseja excluir a despesa '${exp.description}'?",
            onConfirm = {
                onDeleteExpense(exp.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}

@Composable
private fun FinanceBreakdownLine(
    label: String,
    cents: Long,
    valueColor: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
            color = if (bold) WhiteText else GraySecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = CurrencyUtils.formatCents(cents),
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ExpenseFormDialog(
    existing: ExpenseEntity?,
    onSave: (desc: String, cat: String, amountCents: Long, dateMillis: Long, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: "Operacional") }
    var amountText by remember(existing?.id) {
        mutableStateOf(existing?.let { CurrencyUtils.centsToInputString(it.amountCents) } ?: "")
    }
    var dateText by remember(existing?.id) {
        mutableStateOf(DateUtils.formatDate(existing?.dateMillis ?: System.currentTimeMillis()))
    }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        titleContentColor = WhiteText,
        title = { Text(if (existing == null) "Nova Despesa" else "Editar Despesa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; error = null },
                    label = { Text("Descrição *") },
                    placeholder = { Text("Ex.: Aluguel, Embalagens, Fornecedor") },
                    singleLine = true,
                    colors = storeTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it; error = null },
                    label = { Text("Categoria *") },
                    singleLine = true,
                    colors = storeTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; error = null },
                        label = { Text("Valor (R$) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it; error = null },
                        label = { Text("Data *") },
                        placeholder = { Text("DD/MM/AAAA") },
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    colors = storeTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(error!!, color = CoralDanger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cents = CurrencyUtils.parseToCents(amountText)
                    val dateMillis = DateUtils.parseDate(dateText)
                    when {
                        description.isBlank() -> error = "Informe a descrição."
                        category.isBlank() -> error = "Informe a categoria."
                        cents == null || cents <= 0L -> error = "Informe um valor maior que zero."
                        dateMillis == null -> error = "Data inválida. Use DD/MM/AAAA."
                        else -> onSave(description, category, cents, dateMillis, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = GraySecondary)
            }
        }
    )
}
