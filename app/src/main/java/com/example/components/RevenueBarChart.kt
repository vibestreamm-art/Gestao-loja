package com.example.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.DailyChartPoint
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.SubtleBorder
import com.example.utils.CurrencyUtils

@Composable
fun RevenueBarChart(
    points: List<DailyChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxCents = (points.maxOfOrNull { it.valueCents } ?: 0L).coerceAtLeast(100L)
    val totalPeriodCents = points.sumOf { it.valueCents }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Desempenho (Últimos 7 dias)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = CurrencyUtils.formatCents(totalPeriodCents),
                style = MaterialTheme.typography.labelLarge,
                color = GoldPrimary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val count = points.size
            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.48f).coerceAtLeast(10.dp.toPx())
            val baselineY = size.height

            // Subtle baseline
            drawLine(
                color = SubtleBorder,
                start = Offset(0f, baselineY),
                end = Offset(size.width, baselineY),
                strokeWidth = 1.dp.toPx()
            )

            points.forEachIndexed { index, point ->
                val ratio = (point.valueCents.toFloat() / maxCents.toFloat()).coerceIn(0f, 1f)
                val barHeight = if (point.valueCents > 0L) {
                    (ratio * (size.height - 8.dp.toPx())).coerceAtLeast(6.dp.toPx())
                } else {
                    3.dp.toPx()
                }
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = size.height - barHeight

                val brush = if (point.valueCents > 0L) {
                    Brush.verticalGradient(listOf(GoldLight, GoldPrimary))
                } else {
                    Brush.verticalGradient(listOf(SubtleBorder, SubtleBorder))
                }

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = GraySecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        }
    }
}
