package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.components.CustomPeriodDialog
import com.example.components.GoldPrimaryButton
import com.example.components.LuxuryCard
import com.example.components.PeriodFilterRow
import com.example.components.ResultBanner
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.domain.PeriodFilter
import com.example.domain.UiResult
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import com.example.viewmodel.ReportsUiState

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onNavigateBack: () -> Unit,
    onSelectPeriod: (PeriodFilter) -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit,
    onGenerateCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCustomDialog by remember { mutableStateOf(false) }
    var exportFeedback by remember { mutableStateOf<UiResult>(UiResult.Idle) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val csv = onGenerateCsv()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                } ?: error("Não foi possível gravar o arquivo.")
            }.onSuccess {
                exportFeedback = UiResult.Success("Relatório CSV exportado com sucesso!")
            }.onFailure { err ->
                exportFeedback = UiResult.Error(err.message ?: "Erro ao exportar CSV.")
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Relatórios",
                subtitle = if (state.period == PeriodFilter.PERSONALIZADO) {
                    "${DateUtils.formatDate(state.customStartMillis)} - ${DateUtils.formatDate(state.customEndMillis)}"
                } else {
                    state.period.label
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ResultBanner(result = exportFeedback, onDismiss = { exportFeedback = UiResult.Idle })
            }

            item {
                PeriodFilterRow(
                    selectedPeriod = state.period,
                    onSelectPeriod = onSelectPeriod,
                    onOpenCustomRange = { showCustomDialog = true }
                )
            }

            item {
                GoldPrimaryButton(
                    text = "Exportar Relatório em CSV",
                    icon = Icons.Default.Download,
                    onClick = {
                        val stamp = DateUtils.formatDate(System.currentTimeMillis()).replace("/", "-")
                        csvLauncher.launch("relatorio_gestao_store_$stamp.csv")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_csv_button")
                )
            }

            item {
                val f = state.financialSummary
                LuxuryCard(highlightGold = true) {
                    Text("Resumo Financeiro do Período", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportMetricRow("Vendas realizadas", "${f.salesCount}")
                    ReportMetricRow("Faturamento", CurrencyUtils.formatCents(f.salesRevenueCents), GoldLight)
                    ReportMetricRow("Custo dos produtos (CMV)", CurrencyUtils.formatCents(f.cogsCents), AmberWarning)
                    ReportMetricRow("Lucro bruto estimado", CurrencyUtils.formatCents(f.grossProfitCents), EmeraldSuccess)
                    ReportMetricRow("Despesas", CurrencyUtils.formatCents(f.expensesCents), CoralDanger)
                    HorizontalDivider(color = SubtleBorder, modifier = Modifier.padding(vertical = 6.dp))
                    ReportMetricRow(
                        "Saldo líquido registrado",
                        CurrencyUtils.formatCents(f.netBalanceCents),
                        if (f.netBalanceCents >= 0) EmeraldSuccess else CoralDanger
                    )
                }
            }

            item {
                Text("Produtos Mais Vendidos", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }
            if (state.topProducts.isEmpty()) {
                item {
                    LuxuryCard {
                        Text("Nenhuma venda registrada neste período.", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                }
            } else {
                items(state.topProducts.take(10), key = { "top_${it.productId}" }) { tp ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tp.productName, style = MaterialTheme.typography.titleSmall, color = WhiteText)
                                Text("Código: ${tp.productCode}", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${tp.totalQuantity} un.", style = MaterialTheme.typography.titleSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text(CurrencyUtils.formatCents(tp.totalRevenueCents), style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                        }
                    }
                }
            }

            item {
                Text("Produtos com Estoque Baixo (≤ ${state.minStockThreshold} un.)", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }
            if (state.lowStockProducts.isEmpty()) {
                item {
                    LuxuryCard {
                        Text("Nenhum produto com estoque abaixo do mínimo.", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                }
            } else {
                items(state.lowStockProducts, key = { "low_${it.first.product.id}" }) { (pwv, threshold) ->
                    val crit = pwv.variants.filter { it.quantity <= threshold }
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pwv.product.name, style = MaterialTheme.typography.titleSmall, color = WhiteText)
                                Text(
                                    crit.joinToString(" • ") { "${it.color}/${it.size}: ${it.quantity}" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                            StatusBadge("Atenção", AmberWarning, AmberContainer)
                        }
                    }
                }
            }

            item {
                Text("Movimentações no Período (${state.movementsInPeriod.size})", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }
            if (state.movementsInPeriod.isEmpty()) {
                item {
                    LuxuryCard {
                        Text("Sem movimentações no período selecionado.", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                }
            } else {
                items(state.movementsInPeriod.take(25), key = { "mov_${it.id}" }) { mov ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${mov.productNameSnapshot} (${mov.variantLabelSnapshot})", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                                Text("${mov.type.label} • ${DateUtils.formatDateTime(mov.createdAtMillis)}", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                            val deltaStr = if (mov.quantityDelta > 0) "+${mov.quantityDelta}" else mov.quantityDelta.toString()
                            Text(deltaStr, style = MaterialTheme.typography.labelLarge, color = if (mov.quantityDelta >= 0) EmeraldSuccess else CoralDanger)
                        }
                    }
                }
            }
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
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = WhiteText
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = GraySecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}
