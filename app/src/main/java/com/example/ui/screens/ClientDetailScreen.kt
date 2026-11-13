package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.components.ConfirmDeleteDialog
import com.example.components.EmptyStateView
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.StoreTopBar
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.SaleWithItems
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils

@Composable
fun ClientDetailScreen(
    client: ClientEntity?,
    clientSales: List<SaleWithItems>,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onUpdateClient: (fullName: String, phone: String, notes: String) -> Unit,
    onDeleteClient: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("client_detail_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = client?.fullName ?: "Cliente",
                subtitle = client?.phone?.ifBlank { null },
                onNavigateBack = onNavigateBack,
                actions = {
                    if (client != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar cliente", tint = GoldPrimary)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir cliente", tint = CoralDanger)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (client == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cliente não encontrado.", color = GraySecondary)
            }
            return@Scaffold
        }

        val totalSpent = clientSales.sumOf { it.sale.totalCents }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ResultBanner(result = actionResult, onDismiss = onDismissResult) }

            item {
                LuxuryCard(highlightGold = true) {
                    Text(client.fullName, style = MaterialTheme.typography.titleLarge, color = WhiteText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Telefone: ${client.phone.ifBlank { "Não informado" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GraySecondary
                    )
                    Text(
                        text = "Cadastro: ${DateUtils.formatDate(client.createdAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GraySecondary
                    )
                    if (client.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Obs: ${client.notes}", style = MaterialTheme.typography.bodySmall, color = WhiteText)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Compras", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                            Text(clientSales.size.toString(), style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Acumulado", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                            Text(CurrencyUtils.formatCents(totalSpent), style = MaterialTheme.typography.titleMedium, color = GoldLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Histórico de Compras", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }

            if (clientSales.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Sem compras registradas",
                        description = "Quando uma venda for associada a este cliente, ela aparecerá aqui."
                    )
                }
            } else {
                items(clientSales, key = { it.sale.id }) { swi ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Venda #${swi.sale.id} • ${swi.sale.paymentMethod.label}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhiteText
                                )
                                Text(
                                    text = DateUtils.formatDateTime(swi.sale.createdAtMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = swi.items.joinToString(", ") {
                                        "${it.quantity}x ${it.productNameSnapshot} (${it.colorSnapshot}/${it.sizeSnapshot})"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatCents(swi.sale.totalCents),
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && client != null) {
        ClientFormDialog(
            initialClient = client,
            onSave = { name, phone, notes ->
                onUpdateClient(name, phone, notes)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog && client != null) {
        ConfirmDeleteDialog(
            title = "Excluir Cliente",
            message = "Deseja excluir '${client.fullName}'? O histórico de vendas existentes será preservado.",
            onConfirm = {
                showDeleteDialog = false
                onDeleteClient()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
