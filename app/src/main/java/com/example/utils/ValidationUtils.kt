package com.example.utils

object ValidationUtils {
    fun isNotBlank(value: String): Boolean = value.trim().isNotEmpty()

    fun normalizeCode(raw: String): String = raw.trim().uppercase()

    fun sanitizePhone(raw: String): String = raw.trim()

    fun isValidQuantity(raw: String): Int? {
        val parsed = raw.trim().toIntOrNull() ?: return null
        return if (parsed >= 0) parsed else null
    }

    fun isPositiveInt(raw: String): Int? {
        val parsed = raw.trim().toIntOrNull() ?: return null
        return if (parsed > 0) parsed else null
    }
}
