package com.example.myexpenses.feature.analytics.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.core.navigation.route.Args
import com.example.myexpenses.feature.analytics.domain.CategoryGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SubCategoryStat(
    val category: ExpenseCategory,
    val amount: Long,
    val pct: Float, // 0..1 of group total
)

data class CategoryDetailState(
    val group: CategoryGroup? = null,
    val periodLabel: String = "",
    val totalSpend: Long = 0L,
    val trend: TrendSeries = TrendSeries(emptyList(), emptyList(), emptyList()),
    val subCategories: List<SubCategoryStat> = emptyList(),
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>(Args.GROUP_ID) ?: ""
    private val group: CategoryGroup? = CategoryGroup.fromId(groupId)

    val state: StateFlow<CategoryDetailState> = transactionRepository.getAllTransactions()
        .map { all -> buildState(group, all, LocalDate.now()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoryDetailState(group = group),
        )
}

private fun buildState(
    group: CategoryGroup?,
    all: List<Transaction>,
    today: LocalDate,
): CategoryDetailState {
    if (group == null) return CategoryDetailState()

    // Current month bounds
    val first = today.withDayOfMonth(1)
    val last = first.plusMonths(1).minusDays(1)

    val inMonth = all.filter {
        it.type == TransactionType.EXPENSE &&
            it.category in group.children &&
            !it.dateTime.toLocalDate().isBefore(first) &&
            !it.dateTime.toLocalDate().isAfter(last)
    }

    val total = inMonth.sumOf { it.amount }.toLong()

    // Per-day buckets
    val days = first.lengthOfMonth()
    val byDate = inMonth.groupBy { it.dateTime.toLocalDate() }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toLong() }
    val values = (0 until days).map { offset -> byDate[first.plusDays(offset.toLong())] ?: 0L }
    val labels = (1..days).map { it.toString() }
    val ticks = listOf(0, days / 2, days - 1).distinct()
    val trend = TrendSeries(values, labels, ticks)

    // Sub-categories sorted desc
    val subCats = group.children
        .map { cat ->
            val amount = inMonth.filter { it.category == cat }.sumOf { it.amount }.toLong()
            SubCategoryStat(cat, amount, if (total > 0) amount.toFloat() / total else 0f)
        }
        .sortedByDescending { it.amount }

    val periodLabel = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    return CategoryDetailState(
        group = group,
        periodLabel = periodLabel,
        totalSpend = total,
        trend = trend,
        subCategories = subCats,
    )
}
