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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.components.EmptyStateView
import com.example.components.LuxuryCard
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.entities.SaleWithItems
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils

@Composable
fun SaleHistoryScreen(
    sales: List<SaleWithItems>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("sale_history_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Histórico de Vendas",
                subtitle = "${sales.size} venda(s) registrada(s)",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (sales.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "Nenhuma venda realizada",
                    description = "As vendas finalizadas no PDV aparecerão aqui com todos os itens detalhados."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales, key = { it.sale.id }) { swi ->
                    val s = swi.sale
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Venda #${s.id} • ${s.clientNameSnapshot ?: "Consumidor avulso"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhiteText
                                )
                                Text(
                                    text = DateUtils.formatDateTime(s.createdAtMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                            StatusBadge(s.paymentMethod.label, GoldPrimary, GoldContainer)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SubtleBorder)
                        Spacer(modifier = Modifier.height(8.dp))
                        swi.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productNameSnapshot} (${item.colorSnapshot}/${item.sizeSnapshot})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = CurrencyUtils.formatCents(item.lineTotalCents),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WhiteText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (s.discountCents > 0) {
                                Text(
                                    text = "Desc.: ${CurrencyUtils.formatCents(s.discountCents)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GraySecondary
                                )
                            } else {
                                Spacer(modifier = Modifier)
                            }
                            Text(
                                text = "Total: ${CurrencyUtils.formatCents(s.totalCents)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
