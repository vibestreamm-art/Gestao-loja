package com.example.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object DateUtils {
    private val zoneId: ZoneId get() = ZoneId.systemDefault()
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
    private val shortDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM")

    fun formatDate(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().format(dateFormatter)
    }

    fun formatDateTime(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime().format(dateTimeFormatter)
    }

    fun formatShortDate(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().format(shortDateFormatter)
    }

    fun parseDate(dateStr: String): Long? {
        return runCatching {
            val localDate = LocalDate.parse(dateStr.trim(), dateFormatter)
            localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }.getOrNull()
    }

    fun startOfTodayMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        return today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun endOfDayMillis(dayMillis: Long): Long {
        val date = Instant.ofEpochMilli(dayMillis).atZone(zoneId).toLocalDate()
        return date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
    }

    fun startOfDaysAgoMillis(daysAgo: Long, nowMillis: Long = System.currentTimeMillis()): Long {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        return today.minusDays(daysAgo).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun startOfCurrentMonthMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        return today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun previousMonthRangeMillis(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val firstPrev = today.withDayOfMonth(1).minusMonths(1)
        val start = firstPrev.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        return start to end
    }

    fun dayStartMillisForOffset(daysAgo: Long, nowMillis: Long = System.currentTimeMillis()): Pair<Long, String> {
        val date = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate().minusDays(daysAgo)
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli() to date.format(shortDateFormatter)
    }
}
