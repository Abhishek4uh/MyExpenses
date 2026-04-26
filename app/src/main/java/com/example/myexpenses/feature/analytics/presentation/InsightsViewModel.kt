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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// ─── State ─────────────────────────────────────────────────────────────────────

data class TrendSeries(
    val values: List<Long>,
    val labels: List<String>,
    val xTickIndices: List<Int>,
)

data class GroupAmount(
    val group: CategoryGroup,
    val amount: Long,
    val pct: Float,    // 0..1 of total
    val change: Float, // signed pct vs previous period (e.g. 0.32 = +32%)
)

data class IncomeExpense(val income: Long, val expense: Long)

data class InsightsState(
    val range: InsightsRange = InsightsRange.Month,
    val periodLabel: String = "",
    val trend: TrendSeries = TrendSeries(emptyList(), emptyList(), emptyList()),
    val trendDelta: Float = 0f,
    val totalSpend: Long = 0L,
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

    private val _range = MutableStateFlow<InsightsRange>(InsightsRange.Month)
    val range: StateFlow<InsightsRange> = _range.asStateFlow()

    val state: StateFlow<InsightsState> = combine(
        _range,
        transactionRepository.getAllTransactions(),
    ) { range, all ->
        buildState(range, all, LocalDate.now())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InsightsState(),
    )

    init {
        // Restore the persisted range on first launch.
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
}

private fun parseRange(key: String, startEpoch: Long?, endEpoch: Long?): InsightsRange = when (key) {
    "week" -> InsightsRange.Week
    "year" -> InsightsRange.Year
    "custom" -> if (startEpoch != null && endEpoch != null) {
        InsightsRange.Custom(LocalDate.ofEpochDay(startEpoch), LocalDate.ofEpochDay(endEpoch))
    } else InsightsRange.Month
    else -> InsightsRange.Month
}

// ─── Pure aggregation ──────────────────────────────────────────────────────────

private fun buildState(
    range: InsightsRange,
    all: List<Transaction>,
    today: LocalDate,
): InsightsState {
    val (start, end) = range.bounds(today)
    val (prevStart, prevEnd) = range.previousBounds(today)

    val inRange = all.filter {
        val d = it.dateTime.toLocalDate()
        !d.isBefore(start) && !d.isAfter(end)
    }
    val inPrev = all.filter {
        val d = it.dateTime.toLocalDate()
        !d.isBefore(prevStart) && !d.isAfter(prevEnd)
    }

    val curExpenses = inRange.filter { it.type == TransactionType.EXPENSE }
    val prevExpenses = inPrev.filter { it.type == TransactionType.EXPENSE }
    val curIncome = inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.toLong()
    val curExpenseTotal = curExpenses.sumOf { it.amount }.toLong()
    val prevExpenseTotal = prevExpenses.sumOf { it.amount }.toLong()

    val trend = computeTrend(range, curExpenses, start, today)
    val breakdown = computeBreakdown(curExpenses, prevExpenses, curExpenseTotal)

    val daysInRange = (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceAtLeast(1)
    val daysInPrev = (ChronoUnit.DAYS.between(prevStart, prevEnd).toInt() + 1).coerceAtLeast(1)

    val rangeWord = when (range) {
        InsightsRange.Week -> "this week"
        InsightsRange.Month -> "this month"
        InsightsRange.Year -> "this year"
        is InsightsRange.Custom -> "in this range"
    }
    val insights = InsightEngine.generate(
        rangeLabel = rangeWord,
        current = PeriodStats(
            totalExpense = curExpenseTotal,
            totalIncome = curIncome,
            transactionCount = inRange.size,
            daysInRange = daysInRange,
            byGroup = curExpenses.groupBy { CategoryGroup.forCategory(it.category) }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() },
        ),
        previous = PeriodStats(
            totalExpense = prevExpenseTotal,
            totalIncome = inPrev.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.toLong(),
            transactionCount = inPrev.size,
            daysInRange = daysInPrev,
            byGroup = prevExpenses.groupBy { CategoryGroup.forCategory(it.category) }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() },
        ),
    )

    val trendDelta = if (prevExpenseTotal > 0) {
        (curExpenseTotal - prevExpenseTotal).toFloat() / prevExpenseTotal
    } else 0f

    return InsightsState(
        range = range,
        periodLabel = range.displayLabel(today),
        trend = trend,
        trendDelta = trendDelta,
        totalSpend = curExpenseTotal,
        transactionCount = inRange.size,
        breakdown = breakdown,
        incomeExpense = IncomeExpense(curIncome, curExpenseTotal),
        insights = insights,
        showInsights = inRange.size >= 5 && insights.isNotEmpty(),
        showIncomeExpense = !(curIncome == 0L && curExpenseTotal == 0L),
    )
}

private fun computeTrend(
    range: InsightsRange,
    expenses: List<Transaction>,
    start: LocalDate,
    today: LocalDate,
): TrendSeries {
    return when (range) {
        InsightsRange.Year -> {
            // Per-month buckets, Jan..Dec
            val byMonth = expenses.groupBy { it.dateTime.monthValue }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }
            val values = (1..12).map { byMonth[it] ?: 0L }
            val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            TrendSeries(values, labels, listOf(0, 5, 11))
        }
        else -> {
            val days = range.trendBucketCount(today)
            val byDate = expenses.groupBy { it.dateTime.toLocalDate() }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }
            val values = (0 until days).map { offset -> byDate[start.plusDays(offset.toLong())] ?: 0L }
            val labels = if (range is InsightsRange.Week) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            } else {
                val dayFmt = DateTimeFormatter.ofPattern("MMM d")
                values.indices.map { i -> start.plusDays(i.toLong()).format(dayFmt) }
            }
            // Three labels: first, middle, last
            val ticks = listOf(0, days / 2, days - 1).distinct()
            TrendSeries(values, labels, ticks)
        }
    }
}

private fun computeBreakdown(
    current: List<Transaction>,
    previous: List<Transaction>,
    total: Long,
): List<GroupAmount> {
    val curByGroup = current.groupBy { CategoryGroup.forCategory(it.category) }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }
    val prevByGroup = previous.groupBy { CategoryGroup.forCategory(it.category) }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }

    return CategoryGroup.entries.map { group ->
        val amount = curByGroup[group] ?: 0L
        val prev = prevByGroup[group] ?: 0L
        val pct = if (total > 0) amount.toFloat() / total else 0f
        val change = if (prev > 0) (amount - prev).toFloat() / prev else 0f
        GroupAmount(group, amount, pct, change)
    }.sortedByDescending { it.amount }
}
