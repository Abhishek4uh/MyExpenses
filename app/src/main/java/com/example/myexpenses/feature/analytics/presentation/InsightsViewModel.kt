package com.example.myexpenses.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.feature.analytics.domain.CategoryGroup
import com.example.myexpenses.feature.analytics.domain.Insight
import com.example.myexpenses.feature.analytics.domain.InsightEngine
import com.example.myexpenses.feature.analytics.domain.InsightsRange
import com.example.myexpenses.feature.analytics.domain.PeriodStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// ─── State models ──────────────────────────────────────────────────────────────

data class TrendSeries(
    val values: List<Long>,
    val labels: List<String>,
    val xTickIndices: List<Int>,
)

data class GroupAmount(
    val group: CategoryGroup,
    val amount: Long,
    val pct: Float,    // 0..1 of total expense
    val change: Float, // signed pct vs previous period
)

data class IncomeExpense(val income: Long, val expense: Long)

/** Per-day income + expense for the grouped bar chart (Week view). */
data class DayBarData(
    val label: String,  // "Mon", "Tue", …
    val income: Long,
    val expense: Long,
)

data class InsightsState(
    val range: InsightsRange = InsightsRange.Month.current(),
    val periodLabel: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    // Week view
    val weekBars: List<DayBarData> = emptyList(),
    // Month / Year view — two-line chart
    val incomeLineSeries: TrendSeries = TrendSeries(emptyList(), emptyList(), emptyList()),
    val expenseLineSeries: TrendSeries = TrendSeries(emptyList(), emptyList(), emptyList()),
    val trendDelta: Float = 0f,
    val totalSpend: Long = 0L,
    val totalIncome: Long = 0L,
    val transactionCount: Int = 0,
    val breakdown: List<GroupAmount> = emptyList(),
    val incomeExpense: IncomeExpense = IncomeExpense(0, 0),
    val insights: List<Insight> = emptyList(),
    val showInsights: Boolean = false,
    val showIncomeExpense: Boolean = false,
)

// ─── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _range = MutableStateFlow<InsightsRange>(InsightsRange.Month.current())
    val range: StateFlow<InsightsRange> = _range.asStateFlow()

    val state: StateFlow<InsightsState> = combine(
        _range,
        transactionRepository.getAllTransactions(),
        preferencesRepository.getRegistrationEpochMs(),
    ) { range, all, epochMs ->
        val onboardingDate = if (epochMs > 0L) {
            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.now().minusYears(1)
        }
        buildState(range, all, LocalDate.now(), onboardingDate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InsightsState(),
    )

    init {
        viewModelScope.launch {
            val (key, startEpoch, endEpoch) = preferencesRepository.getInsightsRange().first()
            _range.value = parseRange(key, startEpoch, endEpoch)
        }
    }

    fun onRangeChange(newRange: InsightsRange) {
        _range.value = newRange
        viewModelScope.launch {
            val (start, end) = if (newRange is InsightsRange.Custom) {
                newRange.start.toEpochDay() to newRange.end.toEpochDay()
            } else null to null
            preferencesRepository.updateInsightsRange(newRange.key, start, end)
        }
    }

    /** Navigate back or forward one period. No-op if already at the boundary. */
    fun navigatePeriod(forward: Boolean) {
        val today = LocalDate.now()
        // Custom ranges don't support step navigation — user edits via date picker
        if (_range.value is InsightsRange.Custom) return
        _range.value = if (forward) _range.value.navigateForward() else _range.value.navigateBack()
    }
}

// ─── DataStore restore ─────────────────────────────────────────────────────────

private fun parseRange(key: String, startEpoch: Long?, endEpoch: Long?): InsightsRange {
    val today = LocalDate.now()
    return when (key) {
        "week"   -> InsightsRange.Week(today)
        "year"   -> InsightsRange.Year.current()
        "custom" -> if (startEpoch != null && endEpoch != null) {
            InsightsRange.Custom(
                LocalDate.ofEpochDay(startEpoch),
                LocalDate.ofEpochDay(endEpoch),
            )
        } else InsightsRange.Month.current()
        else     -> InsightsRange.Month.current()
    }
}

// ─── Pure aggregation ──────────────────────────────────────────────────────────

private fun buildState(
    range: InsightsRange,
    all: List<Transaction>,
    today: LocalDate,
    onboardingDate: LocalDate,
): InsightsState {
    val (start, end) = range.bounds()
    val (prevStart, prevEnd) = range.previousBounds()

    val inRange = all.filter {
        val d = it.dateTime.toLocalDate()
        !d.isBefore(start) && !d.isAfter(end)
    }
    val inPrev = all.filter {
        val d = it.dateTime.toLocalDate()
        !d.isBefore(prevStart) && !d.isAfter(prevEnd)
    }

    val curExpenses    = inRange.filter { it.type == TransactionType.EXPENSE }
    val prevExpenses   = inPrev.filter  { it.type == TransactionType.EXPENSE }
    val curIncome      = inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.toLong()
    val curExpenseTotal  = curExpenses.sumOf  { it.amount }.toLong()
    val prevExpenseTotal = prevExpenses.sumOf { it.amount }.toLong()

    val breakdown = computeBreakdown(curExpenses, prevExpenses, curExpenseTotal)

    val daysInRange = (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceAtLeast(1)
    val daysInPrev  = (ChronoUnit.DAYS.between(prevStart, prevEnd).toInt() + 1).coerceAtLeast(1)

    val rangeWord = when (range) {
        is InsightsRange.Week   -> "this week"
        is InsightsRange.Month  -> "this month"
        is InsightsRange.Year   -> "this year"
        is InsightsRange.Custom -> "in this range"
    }
    val insights = InsightEngine.generate(
        rangeLabel = rangeWord,
        current = PeriodStats(
            totalExpense = curExpenseTotal,
            totalIncome  = curIncome,
            transactionCount = inRange.size,
            daysInRange  = daysInRange,
            byGroup = curExpenses.groupBy { CategoryGroup.forCategory(it.category) }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() },
        ),
        previous = PeriodStats(
            totalExpense = prevExpenseTotal,
            totalIncome  = inPrev.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.toLong(),
            transactionCount = inPrev.size,
            daysInRange  = daysInPrev,
            byGroup = prevExpenses.groupBy { CategoryGroup.forCategory(it.category) }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() },
        ),
    )

    val trendDelta = if (prevExpenseTotal > 0)
        (curExpenseTotal - prevExpenseTotal).toFloat() / prevExpenseTotal
    else 0f

    // Week → grouped bar chart
    val weekBars = if (range is InsightsRange.Week) {
        computeWeekBars(range, inRange)
    } else emptyList()

    // Month / Year → two-line chart
    val (incomeLine, expenseLine) = if (range is InsightsRange.Week) {
        TrendSeries(emptyList(), emptyList(), emptyList()) to
            TrendSeries(emptyList(), emptyList(), emptyList())
    }
    else {
        computeLineSeries(range, inRange, start)
    }

    return InsightsState(
        range            = range,
        periodLabel      = range.displayLabel(),
        canGoBack        = range.canGoBack(onboardingDate),
        canGoForward     = range.canGoForward(today),
        weekBars         = weekBars,
        incomeLineSeries = incomeLine,
        expenseLineSeries = expenseLine,
        trendDelta       = trendDelta,
        totalSpend       = curExpenseTotal,
        totalIncome      = curIncome,
        transactionCount = inRange.size,
        breakdown        = breakdown,
        incomeExpense    = IncomeExpense(curIncome, curExpenseTotal),
        insights         = insights,
        showInsights     = inRange.size >= 3 && insights.isNotEmpty(),
        showIncomeExpense = !(curIncome == 0L && curExpenseTotal == 0L),
    )
}

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun computeWeekBars(
    range: InsightsRange.Week,
    inRange: List<Transaction>,
): List<DayBarData> {
    val start = range.endDate.minusDays(6)
    val byDate = inRange.groupBy { it.dateTime.toLocalDate() }
    return (0..6).map { offset ->
        val date  = start.plusDays(offset.toLong())
        val txs   = byDate[date] ?: emptyList()
        val inc   = txs.filter { it.type == TransactionType.INCOME }.sumOf  { it.amount }.toLong()
        val exp   = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }.toLong()
        val dow   = date.dayOfWeek.value - 1   // Mon=0 … Sun=6
        DayBarData(DAY_LABELS[dow], inc, exp)
    }
}

private fun computeLineSeries(
    range: InsightsRange,
    inRange: List<Transaction>,
    start: LocalDate,
): Pair<TrendSeries, TrendSeries> {
    return when (range) {
        is InsightsRange.Year -> {
            val byMonth = inRange.groupBy { it.dateTime.monthValue }
            val income  = (1..12).map { m ->
                byMonth[m]?.filter { it.type == TransactionType.INCOME }?.sumOf { it.amount }?.toLong() ?: 0L
            }
            val expense = (1..12).map { m ->
                byMonth[m]?.filter { it.type == TransactionType.EXPENSE }?.sumOf { it.amount }?.toLong() ?: 0L
            }
            val labels = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val ticks  = listOf(0, 5, 11)
            TrendSeries(income, labels, ticks) to TrendSeries(expense, labels, ticks)
        }
        is InsightsRange.Month -> {
            val days   = LocalDate.of(range.year, range.month, 1).lengthOfMonth()
            val byDate = inRange.groupBy { it.dateTime.toLocalDate() }
            val income  = (0 until days).map { i ->
                byDate[start.plusDays(i.toLong())]
                    ?.filter { it.type == TransactionType.INCOME }?.sumOf { it.amount }?.toLong() ?: 0L
            }
            val expense = (0 until days).map { i ->
                byDate[start.plusDays(i.toLong())]
                    ?.filter { it.type == TransactionType.EXPENSE }?.sumOf { it.amount }?.toLong() ?: 0L
            }
            val fmt    = DateTimeFormatter.ofPattern("d")
            val labels = (0 until days).map { start.plusDays(it.toLong()).format(fmt) }
            val ticks  = listOf(0, days / 2, days - 1).distinct()
            TrendSeries(income, labels, ticks) to TrendSeries(expense, labels, ticks)
        }
        else -> TrendSeries(emptyList(), emptyList(), emptyList()) to
            TrendSeries(emptyList(), emptyList(), emptyList())
    }
}

private fun computeBreakdown(
    current: List<Transaction>,
    previous: List<Transaction>,
    total: Long,
): List<GroupAmount> {
    val curByGroup  = current.groupBy  { CategoryGroup.forCategory(it.category) }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }
    val prevByGroup = previous.groupBy { CategoryGroup.forCategory(it.category) }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }

    return CategoryGroup.entries.map { group ->
        val amount = curByGroup[group]  ?: 0L
        val prev   = prevByGroup[group] ?: 0L
        val pct    = if (total > 0) amount.toFloat() / total else 0f
        val change = if (prev > 0) (amount - prev).toFloat() / prev else 0f
        GroupAmount(group, amount, pct, change)
    }.sortedByDescending { it.amount }
}
