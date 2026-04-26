package com.example.myexpenses.feature.analytics.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * The four selectable time ranges on the Insights screen. Persisted to
 * DataStore by [com.example.myexpenses.core.data.PreferencesRepository] —
 * key `insights_range` plus optional `insights_custom_start` /
 * `insights_custom_end` epoch days when [Custom] is selected.
 */
sealed class InsightsRange(val key: String) {
    data object Week : InsightsRange("week")
    data object Month : InsightsRange("month")
    data object Year : InsightsRange("year")
    data class Custom(val start: LocalDate, val end: LocalDate) : InsightsRange("custom")

    /** Inclusive [start, end] dates of the range relative to [today]. */
    fun bounds(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        Week -> {
            val mon = today.with(DayOfWeek.MONDAY)
            mon to mon.plusDays(6)
        }
        Month -> {
            val first = today.withDayOfMonth(1)
            first to first.plusMonths(1).minusDays(1)
        }
        Year -> LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
        is Custom -> start to end
    }

    /**
     * Bounds of the *previous* equivalent period — used by InsightEngine to
     * compare current vs prior totals (e.g. "spending up 12% this month").
     */
    fun previousBounds(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        Week -> {
            val mon = today.with(DayOfWeek.MONDAY).minusWeeks(1)
            mon to mon.plusDays(6)
        }
        Month -> {
            val first = today.withDayOfMonth(1).minusMonths(1)
            first to first.plusMonths(1).minusDays(1)
        }
        Year -> LocalDate.of(today.year - 1, 1, 1) to LocalDate.of(today.year - 1, 12, 31)
        is Custom -> {
            // Slide the same length window backwards
            val span = ChronoUnit.DAYS.between(start, end)
            val prevEnd = start.minusDays(1)
            val prevStart = prevEnd.minusDays(span)
            prevStart to prevEnd
        }
    }

    /** "March 2026", "This week", "Mar 1 — Mar 14" — used in headers/sub-titles. */
    fun displayLabel(today: LocalDate): String = when (this) {
        Week -> "This week"
        Month -> today.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        Year -> today.year.toString()
        is Custom -> {
            val fmt = DateTimeFormatter.ofPattern("MMM d")
            "${start.format(fmt)} — ${end.format(fmt)}"
        }
    }

    /** Short label for the segmented filter when this range is Custom. */
    fun shortLabel(): String = when (this) {
        Week -> "Week"
        Month -> "Month"
        Year -> "Year"
        is Custom -> {
            val fmt = DateTimeFormatter.ofPattern("MMM d")
            "${start.format(fmt)}—${end.format(fmt)}"
        }
    }

    /** Number of buckets the trend chart should render. */
    fun trendBucketCount(today: LocalDate): Int = when (this) {
        Week -> 7
        Month -> bounds(today).first.lengthOfMonth()
        Year -> 12
        is Custom -> (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceAtLeast(1)
    }
}
