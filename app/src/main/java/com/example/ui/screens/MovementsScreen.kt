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
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.domain.MovementType
import com.example.entities.StockMovementEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.WhiteText
import com.example.utils.DateUtils

@Composable
fun MovementsScreen(
    movements: List<StockMovementEntity>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("movements_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Movimentações",
                subtitle = "Histórico de entradas, saídas e ajustes",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (movements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                EmptyStateView(
                    icon = Icons.Default.SwapHoriz,
                    title = "Sem movimentações",
                    description = "Entradas de estoque, vendas e ajustes manuais aparecerão aqui automaticamente."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(movements, key = { it.id }) { mov ->
                    val (fg, bg) = when (mov.type) {
                        MovementType.ENTRADA -> EmeraldSuccess to EmeraldContainer
                        MovementType.SAIDA_VENDA -> CoralDanger to CoralContainer
                        MovementType.AJUSTE -> AmberWarning to AmberContainer
                        MovementType.ESTORNO -> GoldPrimary to GoldContainer
                    }
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mov.productNameSnapshot,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhiteText
                                )
                                Text(
                                    text = "${mov.variantLabelSnapshot} • ${DateUtils.formatDateTime(mov.createdAtMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mov.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(mov.type.label, fg, bg)
                                Spacer(modifier = Modifier.height(6.dp))
                                val deltaText = if (mov.quantityDelta > 0) "+${mov.quantityDelta}" else mov.quantityDelta.toString()
                                Text(
                                    text = "$deltaText un. (Saldo: ${mov.newQuantity})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WhiteText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
