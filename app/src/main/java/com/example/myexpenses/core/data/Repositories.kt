package com.example.myexpenses.core.data

import com.example.myexpenses.core.common.CategoryBreakdown
import com.example.myexpenses.core.common.DailyAggregate
import com.example.myexpenses.core.common.DashboardStats
import com.example.myexpenses.core.common.MonthlyAggregate
import com.example.myexpenses.core.common.PendingSmsTransaction
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.common.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime


interface TransactionRepository {

    // ─── CRUD ────────────────────────────────────────────────────────────────

    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun deleteAllTransactions()
    fun getTransactionById(id: String): Flow<Transaction?>

    // ─── Queries ──────────────────────────────────────────────────────────────

    fun getAllTransactions(): Flow<List<Transaction>>

    fun getTransactionsForDay(date: LocalDateTime): Flow<List<Transaction>>

    fun getTransactionsForWeek(
        startOfWeek: LocalDateTime,
        endOfWeek: LocalDateTime
    ): Flow<List<Transaction>>

    fun getTransactionsForMonth(
        month: Int,
        year: Int
    ): Flow<List<Transaction>>

    fun getTransactionsForYear(year: Int): Flow<List<Transaction>>

    // ─── Aggregates ───────────────────────────────────────────────────────────

    fun getDashboardStats(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<DashboardStats>

    fun getCategoryBreakdown(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Flow<List<CategoryBreakdown>>

    fun getDailyAggregates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DailyAggregate>>

    fun getMonthlyAggregates(year: Int): Flow<List<MonthlyAggregate>>

    // ─── Pending SMS Transactions ─────────────────────────────────────────────

    fun getPendingSmsTransactions(): Flow<List<PendingSmsTransaction>>
    suspend fun insertPendingSmsTransaction(pending: PendingSmsTransaction)
    suspend fun deletePendingSmsTransaction(id: String)
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateDarkTheme(isDark: Boolean)
    suspend fun updateSmsReaderEnabled(enabled: Boolean)
    suspend fun updateRemindersEnabled(enabled: Boolean)
    suspend fun updateName(name: String)
    suspend fun setOnboardingComplete()

    // ─── Insights screen range ─────────────────────────────────────────────────
    /**
     * Triple of (rangeKey, customStartEpochDay?, customEndEpochDay?). Custom
     * dates are populated only when [rangeKey] equals "custom".
     */
    fun getInsightsRange(): Flow<Triple<String, Long?, Long?>>
    suspend fun updateInsightsRange(rangeKey: String, customStartEpochDay: Long?, customEndEpochDay: Long?)
}
