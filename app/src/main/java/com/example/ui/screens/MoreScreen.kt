package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.components.LuxuryCard
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.WhiteText

@Composable
fun MoreScreen(
    onOpenOrders: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMovements: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("more_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Mais Opções",
                    style = MaterialTheme.typography.headlineMedium,
                    color = WhiteText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Acesse gestão de pedidos, financeiro, relatórios e backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GraySecondary
                )
            }
        }

        item {
            MoreMenuRow(
                title = "Pedidos & Encomendas",
                subtitle = "Reservas, encomendas sob medida, sinal pago e entregas",
                icon = Icons.Default.LocalShipping,
                testTag = "more_orders_item",
                onClick = onOpenOrders
            )
        }

        item {
            MoreMenuRow(
                title = "Financeiro",
                subtitle = "Faturamento, CMV, lucro bruto estimado, despesas e saldo",
                icon = Icons.Default.AccountBalanceWallet,
                testTag = "more_finance_item",
                onClick = onOpenFinance
            )
        }

        item {
            MoreMenuRow(
                title = "Relatórios & Exportação CSV",
                subtitle = "Vendas por período, mais vendidos, estoque baixo e CSV",
                icon = Icons.Default.Assessment,
                testTag = "more_reports_item",
                onClick = onOpenReports
            )
        }

        item {
            MoreMenuRow(
                title = "Movimentações de Estoque",
                subtitle = "Auditoria de entradas, saídas de vendas e ajustes",
                icon = Icons.Default.SwapHoriz,
                testTag = "more_movements_item",
                onClick = onOpenMovements
            )
        }

        item {
            MoreMenuRow(
                title = "Configurações & Backup",
                subtitle = "Dados da loja, estoque mínimo, backup e restauração local",
                icon = Icons.Default.Settings,
                testTag = "more_settings_item",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun MoreMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    LuxuryCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(GoldContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = WhiteText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GraySecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GoldPrimary
            )
        }
    }
}
