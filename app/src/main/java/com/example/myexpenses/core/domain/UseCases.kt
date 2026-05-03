package com.example.myexpenses.core.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.myexpenses.core.common.CategoryBreakdown
import com.example.myexpenses.core.common.DailyAggregate
import com.example.myexpenses.core.common.DashboardStats
import com.example.myexpenses.core.common.MonthlyAggregate
import com.example.myexpenses.core.common.PendingSmsTransaction
import com.example.myexpenses.core.common.StreakData
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.TransactionDao
import com.example.myexpenses.core.data.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import timber.log.Timber

// ─── Add Transaction ──────────────────────────────────────────────────────────

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        require(transaction.amount > 0) { "Amount must be greater than zero" }
        repository.insertTransaction(transaction)
    }
}

// ─── Get Dashboard Stats ──────────────────────────────────────────────────────

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    fun forToday(): Flow<DashboardStats> {
        val now = LocalDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1).minusNanos(1)
        return repository.getDashboardStats(startOfDay, endOfDay)
    }

    fun forCurrentWeek(): Flow<DashboardStats> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val today = LocalDate.now()
        val startOfWeek = today.with(weekFields.dayOfWeek(), 1).atStartOfDay()
        val endOfWeek = startOfWeek.plusDays(6).plusHours(23).plusMinutes(59)
        return repository.getDashboardStats(startOfWeek, endOfWeek)
    }

    fun forCurrentMonth(): Flow<DashboardStats> {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59)
        return repository.getDashboardStats(startOfMonth, endOfMonth)
    }

    fun forYear(year: Int): Flow<DashboardStats> {
        val startOfYear = LocalDateTime.of(year, 1, 1, 0, 0)
        val endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59)
        return repository.getDashboardStats(startOfYear, endOfYear)
    }
}

// ─── Get Weekly Chart Data ────────────────────────────────────────────────────

class GetWeeklyChartDataUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<DailyAggregate>> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val today = LocalDate.now()
        val startOfWeek = today.with(weekFields.dayOfWeek(), 1).atStartOfDay()
        val endOfWeek = startOfWeek.plusDays(6).plusHours(23).plusMinutes(59)
        return repository.getDailyAggregates(startOfWeek, endOfWeek)
    }
}

// ─── Get Monthly Chart Data ───────────────────────────────────────────────────

class GetMonthlyChartDataUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<List<DailyAggregate>> {
        val start = LocalDateTime.of(year, month, 1, 0, 0)
        val end = start.withDayOfMonth(
            LocalDate.of(year, month, 1).lengthOfMonth()
        ).withHour(23).withMinute(59)
        return repository.getDailyAggregates(start, end)
    }
}

// ─── Get Yearly Chart Data ────────────────────────────────────────────────────

class GetYearlyChartDataUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(year: Int): Flow<List<MonthlyAggregate>> {
        return repository.getMonthlyAggregates(year)
    }
}

// ─── Get Category Breakdown ───────────────────────────────────────────────────

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    fun forCurrentMonth(type: TransactionType): Flow<List<CategoryBreakdown>> {
        val now = LocalDate.now()
        val start = now.withDayOfMonth(1).atStartOfDay()
        val end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59)
        return repository.getCategoryBreakdown(start, end, type)
    }
}

// ─── Get Recent Transactions ──────────────────────────────────────────────────

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> =
        repository.getAllTransactions()
}

// ─── Get Transaction By ID ────────────────────────────────────────────────────

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(id: String): Flow<Transaction?> =
        repository.getTransactionById(id)
}

// ─── Delete Transaction ───────────────────────────────────────────────────────

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteTransaction(id)
    }
}

// ─── Get Streak ───────────────────────────────────────────────────────────────

@Singleton
class GetStreakUseCase @Inject constructor(
    private val dao: TransactionDao,
    private val prefs: PreferencesRepository,
) {
    operator fun invoke(): Flow<StreakData> = combine(
        dao.getDistinctActiveDays(),
        prefs.getRegistrationEpochMs(),
    ) { dayStrings, regMs ->
        val activeDays = dayStrings.mapNotNullTo(mutableSetOf()) {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }
        val onboardingDate = Instant.ofEpochMilli(regMs.coerceAtLeast(1L))
            .atZone(ZoneId.systemDefault()).toLocalDate()
        StreakData(
            currentStreak = computeStreak(activeDays),
            activeDays    = activeDays,
            onboardingDate = onboardingDate,
        )
    }

    private fun computeStreak(activeDays: Set<LocalDate>): Int {
        val today = LocalDate.now()
        if (today !in activeDays && today.minusDays(1) !in activeDays) return 0
        var streak = 0
        var day = if (today in activeDays) today else today.minusDays(1)
        while (day in activeDays) { streak++; day = day.minusDays(1) }
        return streak
    }
}

// ─── Confirm Pending SMS Transaction ─────────────────────────────────────────

class ConfirmSmsTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        pending: PendingSmsTransaction,
        confirmedTransaction: Transaction
    ) {
        repository.insertTransaction(confirmedTransaction)
        repository.deletePendingSmsTransaction(pending.id)
    }
}


fun Context.openUrl(url: String){
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
        Timber.tag("DEBUG").d("Opened URL: $url")
    }
    catch (e: Exception) {
        Timber.tag("DEBUG").d("Failed due to ${e.stackTrace} - ${e.cause} - ${e.message}")
    }
}