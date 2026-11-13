package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.components.EmptyStateView
import com.example.components.GoldPrimaryButton
import com.example.components.KpiCard
import com.example.components.LuxuryCard
import com.example.components.RevenueBarChart
import com.example.components.StatusBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import com.example.viewmodel.DashboardUiState

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onOpenNewSale: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenProductDetail: (Long) -> Unit,
    onOpenSaleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldContainer.copy(alpha = 0.55f), CharcoalSurface)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.storeName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = state.businessInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusBadge(
                                label = DateUtils.formatDate(System.currentTimeMillis()),
                                textColor = GoldPrimary,
                                containerColor = DeepBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Faturamento do Mês",
                            style = MaterialTheme.typography.labelMedium,
                            color = GraySecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCents(state.monthRevenueCents),
                            style = MaterialTheme.typography.displaySmall,
                            color = WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GoldPrimaryButton(
                            text = "Nova Venda",
                            icon = Icons.Default.AddShoppingCart,
                            onClick = onOpenNewSale,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_new_sale_button")
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Vendas Hoje",
                    value = CurrencyUtils.formatCents(state.todayRevenueCents),
                    subtitle = "${state.todaySalesCount} venda(s) no dia",
                    icon = Icons.Default.PointOfSale,
                    accentColor = EmeraldSuccess,
                    highlight = true,
                    onClick = onOpenSaleHistory,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pedidos Pendentes",
                    value = state.pendingOrdersCount.toString(),
                    subtitle = "Em aberto / andamento",
                    icon = Icons.Default.LocalShipping,
                    accentColor = AmberWarning,
                    onClick = onOpenOrders,
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
                    title = "Produtos",
                    value = state.totalProducts.toString(),
                    subtitle = "Modelos cadastrados",
                    icon = Icons.Default.Checkroom,
                    accentColor = GoldPrimary,
                    onClick = onOpenInventory,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Peças em Estoque",
                    value = state.totalStockItems.toString(),
                    subtitle = "Total de unidades",
                    icon = Icons.Default.Inventory2,
                    accentColor = GoldLight,
                    onClick = onOpenInventory,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            LuxuryCard {
                RevenueBarChart(points = state.last7DaysChart)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (state.lowStockProducts.isNotEmpty()) AmberWarning else GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estoque Baixo (≤ ${state.lowStockThreshold} un.)",
                        style = MaterialTheme.typography.titleMedium,
                        color = WhiteText
                    )
                }
                TextButton(onClick = onOpenInventory) {
                    Text("Ver estoque", color = GoldPrimary)
                }
            }
        }

        if (state.lowStockProducts.isEmpty()) {
            item {
                LuxuryCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (state.totalProducts == 0) "Nenhum produto cadastrado ainda" else "Estoque saudável",
                                style = MaterialTheme.typography.titleSmall,
                                color = WhiteText
                            )
                            Text(
                                text = if (state.totalProducts == 0) {
                                    "Cadastre suas peças no módulo Estoque para monitorar variações."
                                } else {
                                    "Nenhuma variação abaixo do limite mínimo configurado."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = GraySecondary
                            )
                        }
                    }
                }
            }
        } else {
            items(state.lowStockProducts.take(5), key = { "low_${it.product.id}" }) { pwv ->
                val criticalVariants = pwv.variants.filter { it.quantity <= state.lowStockThreshold }
                LuxuryCard(onClick = { onOpenProductDetail(pwv.product.id) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pwv.product.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = WhiteText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = criticalVariants.joinToString(" • ") {
                                    "${it.color}/${it.size}: ${it.quantity}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = GraySecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        val hasZero = criticalVariants.any { it.quantity == 0 }
                        StatusBadge(
                            label = if (hasZero) "Esgotado" else "Baixo",
                            textColor = if (hasZero) CoralDanger else AmberWarning,
                            containerColor = if (hasZero) CoralContainer else AmberContainer
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimas Vendas Realizadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = WhiteText
                )
                TextButton(onClick = onOpenSaleHistory) {
                    Text("Histórico", color = GoldPrimary)
                }
            }
        }

        if (state.recentSales.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "Nenhuma venda registrada",
                    description = "Suas vendas concluídas aparecerão aqui com os indicadores atualizados em tempo real.",
                    actionLabel = "Registrar venda",
                    onAction = onOpenNewSale
                )
            }
        } else {
            items(state.recentSales, key = { "sale_${it.sale.id}" }) { swi ->
                LuxuryCard(onClick = onOpenSaleHistory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = swi.sale.clientNameSnapshot ?: "Consumidor avulso",
                                style = MaterialTheme.typography.titleSmall,
                                color = WhiteText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${DateUtils.formatDateTime(swi.sale.createdAtMillis)} • ${swi.sale.paymentMethod.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GraySecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatCents(swi.sale.totalCents),
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${swi.items.sumOf { it.quantity }} peça(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = GraySecondary
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
