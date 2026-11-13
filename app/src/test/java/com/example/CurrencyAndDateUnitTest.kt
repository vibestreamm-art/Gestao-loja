package com.example

import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyAndDateUnitTest {

    @Test
    fun `currency formats and parses Brazilian Real correctly`() {
        assertEquals("R$ 1.250,90", CurrencyUtils.formatCents(125090L))
        assertEquals("-R$ 45,00", CurrencyUtils.formatCents(-4500L))
        assertEquals(12990L, CurrencyUtils.parseToCents("129,90"))
        assertEquals(125050L, CurrencyUtils.parseToCents("R$ 1.250,50"))
        assertEquals(8990L, CurrencyUtils.parseToCents("89.90"))
        assertNull(CurrencyUtils.parseToCents("abc"))
        assertNull(CurrencyUtils.parseToCents("-10,00"))
    }

    @Test
    fun `date parses strict DD_MM_YYYY and formats back`() {
        val millis = DateUtils.parseDate("13/11/2026")
        assertNotNull(millis)
        assertEquals("13/11/2026", DateUtils.formatDate(millis!!))
        assertNull(DateUtils.parseDate("31/02/2026"))
        assertNull(DateUtils.parseDate("2026-11-13"))
    }
}
