package com.example.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyUtils {
    private val ptBrLocale = Locale("pt", "BR")
    private val symbols = DecimalFormatSymbols(ptBrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun formatCents(cents: Long): String {
        val isNegative = cents < 0
        val absValue = kotlin.math.abs(cents) / 100.0
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(absValue)
        return if (isNegative) "-R$ $formatted" else "R$ $formatted"
    }

    fun centsToInputString(cents: Long): String {
        if (cents <= 0L) return ""
        val value = cents / 100.0
        val formatter = DecimalFormat("0.00", symbols)
        return formatter.format(value)
    }

    /**
     * Parses Brazilian or international currency text (e.g., "129,90", "1.290,50", "129.90", "R$ 50,00")
     * into non-negative cents, or null if invalid.
     */
    fun parseToCents(raw: String): Long? {
        val cleaned = raw
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
            .trim()
        if (cleaned.isEmpty()) return null

        val normalized = when {
            cleaned.contains(",") && cleaned.contains(".") -> {
                // Assume Brazilian 1.234,56 if last separator is comma
                if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                    cleaned.replace(".", "").replace(",", ".")
                } else {
                    cleaned.replace(",", "")
                }
            }
            cleaned.contains(",") -> cleaned.replace(",", ".")
            else -> cleaned
        }

        val parsed = normalized.toDoubleOrNull() ?: return null
        if (parsed < 0.0 || parsed.isNaN() || parsed.isInfinite()) return null
        return (parsed * 100.0).roundToLong()
    }
}
