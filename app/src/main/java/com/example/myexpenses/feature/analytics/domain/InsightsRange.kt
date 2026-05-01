package com.example.myexpenses.feature.analytics.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * The four selectable time ranges on the Insights screen.
 *
 * Week/Month/Year carry their own anchor so the user can navigate back/forward
 * within each tab without needing external state.
 *
 * Persisted to DataStore by [com.example.myexpenses.core.data.PreferencesRepository]:
 *   key `insights_range` + optional `insights_custom_start` / `insights_custom_end`
 *   epoch-days when [Custom] is selected. Week/Month/Year always reset to the
 *   current period on a fresh launch (navigation state is session-only).
 */
sealed class InsightsRange(val key: String) {

    /** Rolling 7-day window whose last day is [endDate]. Default = today. */
    data class Week(val endDate: LocalDate = LocalDate.now()) : InsightsRange("week")

    /** A specific calendar month. */
    data class Month(val year: Int, val month: Int) : InsightsRange("month") {
        companion object {
            fun current(): Month = LocalDate.now().let { Month(it.year, it.monthValue) }
        }
    }

    /** A specific calendar year. */
    data class Year(val year: Int) : InsightsRange("year") {
        companion object {
            fun current(): Year = Year(LocalDate.now().year)
        }
    }

    data class Custom(val start: LocalDate, val end: LocalDate) : InsightsRange("custom")

    // ─── Bounds ───────────────────────────────────────────────────────────────

    /** Inclusive [start, end] dates for this range. */
    fun bounds(): Pair<LocalDate, LocalDate> = when (this) {
        is Week  -> endDate.minusDays(6) to endDate
        is Month -> {
            val first = LocalDate.of(year, month, 1)
            first to first.plusMonths(1).minusDays(1)
        }
        is Year  -> LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
        is Custom -> start to end
    }

    /** Bounds of the *previous* equivalent period — used by InsightEngine for comparisons. */
    fun previousBounds(): Pair<LocalDate, LocalDate> = when (this) {
        is Week  -> {
            val prevEnd = endDate.minusDays(7)
            prevEnd.minusDays(6) to prevEnd
        }
        is Month -> {
            val prevFirst = LocalDate.of(year, month, 1).minusMonths(1)
            prevFirst to prevFirst.plusMonths(1).minusDays(1)
        }
        is Year  -> LocalDate.of(year - 1, 1, 1) to LocalDate.of(year - 1, 12, 31)
        is Custom -> {
            val span = ChronoUnit.DAYS.between(start, end)
            val prevEnd = start.minusDays(1)
            prevEnd.minusDays(span) to prevEnd
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    fun navigateBack(): InsightsRange = when (this) {
        is Week  -> Week(endDate.minusDays(7))
        is Month -> {
            val prev = LocalDate.of(year, month, 1).minusMonths(1)
            Month(prev.year, prev.monthValue)
        }
        is Year  -> Year(year - 1)
        is Custom -> {
            val span = ChronoUnit.DAYS.between(start, end) + 1
            Custom(start.minusDays(span), end.minusDays(span))
        }
    }

    fun navigateForward(): InsightsRange = when (this) {
        is Week  -> Week(endDate.plusDays(7))
        is Month -> {
            val next = LocalDate.of(year, month, 1).plusMonths(1)
            Month(next.year, next.monthValue)
        }
        is Year  -> Year(year + 1)
        is Custom -> {
            val span = ChronoUnit.DAYS.between(start, end) + 1
            Custom(start.plusDays(span), end.plusDays(span))
        }
    }

    // ─── Constraints ──────────────────────────────────────────────────────────

    /** True if a previous period exists at or after [onboardingDate]. */
    fun canGoBack(onboardingDate: LocalDate): Boolean = when (this) {
        is Week  -> endDate.minusDays(7) >= onboardingDate
        is Month -> {
            val prevFirst = LocalDate.of(year, month, 1).minusMonths(1)
            !prevFirst.isBefore(onboardingDate.withDayOfMonth(1))
        }
        is Year  -> year - 1 >= onboardingDate.year
        is Custom -> {
            val span = ChronoUnit.DAYS.between(start, end) + 1
            !start.minusDays(span).isBefore(onboardingDate)
        }
    }

    /** True if a future period exists on or before [today]. */
    fun canGoForward(today: LocalDate = LocalDate.now()): Boolean = when (this) {
        is Week  -> endDate < today
        is Month -> LocalDate.of(year, month, 1).isBefore(today.withDayOfMonth(1))
        is Year  -> year < today.year
        is Custom -> end < today
    }

    // ─── Display ──────────────────────────────────────────────────────────────

    /**
     * V3 format:
     *  Week   → "18-Apr-2026 – 24-Apr-2026"
     *  Month  → "April 2026"
     *  Year   → "2026"
     *  Custom → "18-Apr-2026 – 24-Apr-2026"
     */
    fun displayLabel(): String = when (this) {
        is Week  -> {
            val fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
            "${endDate.minusDays(6).format(fmt)}  –  ${endDate.format(fmt)}"
        }
        is Month -> {
            LocalDate.of(year, month, 1)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
        is Year  -> year.toString()
        is Custom -> {
            val fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
            "${start.format(fmt)} – ${end.format(fmt)}"
        }
    }

    /** Short label for the segmented tab filter. */
    fun shortLabel(): String = when (this) {
        is Week   -> "Week"
        is Month  -> "Month"
        is Year   -> "Year"
        is Custom -> {
            val fmt = DateTimeFormatter.ofPattern("MMM d")
            "${start.format(fmt)}—${end.format(fmt)}"
        }
    }

    /** Number of buckets the trend chart should render. */
    fun trendBucketCount(): Int = when (this) {
        is Week   -> 7
        is Month  -> LocalDate.of(year, month, 1).lengthOfMonth()
        is Year   -> 12
        is Custom -> (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceAtLeast(1)
    }
}
