package com.example.myexpenses.core.data

import com.example.myexpenses.core.common.CategoryBreakdown
import com.example.myexpenses.core.common.DailyAggregate
import com.example.myexpenses.core.common.DashboardStats
import com.example.myexpenses.core.common.MonthlyAggregate
import com.example.myexpenses.core.common.PendingSmsTransaction
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val pendingSmsDao: PendingSmsDao
) : TransactionRepository {

    override suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insert(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    override suspend fun deleteTransaction(id: String) =
        transactionDao.deleteById(id)

    override suspend fun deleteAllTransactions() =
        transactionDao.deleteAll()

    override fun getTransactionById(id: String): Flow<Transaction?> =
        transactionDao.getById(id).map { it?.toDomain() }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsForDay(date: LocalDateTime): Flow<List<Transaction>> {
        val start = date.toLocalDate().atStartOfDay().toEpochMilli()
        val end = date.toLocalDate().atTime(23, 59, 59).toEpochMilli()
        return transactionDao.getByDateRange(start, end).map { it.map { e -> e.toDomain() } }
    }

    override fun getTransactionsForWeek(
        startOfWeek: LocalDateTime,
        endOfWeek: LocalDateTime
    ): Flow<List<Transaction>> =
        transactionDao.getByDateRange(
            startOfWeek.toEpochMilli(),
            endOfWeek.toEpochMilli()
        ).map { it.map { e -> e.toDomain() } }

    override fun getTransactionsForMonth(month: Int, year: Int): Flow<List<Transaction>> {
        val start = LocalDateTime.of(year, month, 1, 0, 0).toEpochMilli()
        val end = LocalDateTime.of(year, month, daysInMonth(month, year), 23, 59, 59).toEpochMilli()
        return transactionDao.getByDateRange(start, end).map { it.map { e -> e.toDomain() } }
    }

    override fun getTransactionsForYear(year: Int): Flow<List<Transaction>> {
        val start = LocalDateTime.of(year, 1, 1, 0, 0).toEpochMilli()
        val end = LocalDateTime.of(year, 12, 31, 23, 59, 59).toEpochMilli()
        return transactionDao.getByDateRange(start, end).map { it.map { e -> e.toDomain() } }
    }

    override fun getDashboardStats(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<DashboardStats> =
        transactionDao.getIncomeExpenseTotals(
            startDate.toEpochMilli(),
            endDate.toEpochMilli()
        ).map { totals ->
            DashboardStats(
                totalIncome = totals.income,
                totalExpense = totals.expense
            )
        }

    override fun getCategoryBreakdown(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Flow<List<CategoryBreakdown>> =
        transactionDao.getCategoryTotals(
            type = type.name,
            startMs = startDate.toEpochMilli(),
            endMs = endDate.toEpochMilli()
        ).map { it.toDomainBreakdown(type) }

    override fun getDailyAggregates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DailyAggregate>> =
        transactionDao.getDailyAggregates(
            startDate.toEpochMilli(),
            endDate.toEpochMilli()
        ).map { list -> list.map { it.toDomain() } }

    override fun getMonthlyAggregates(year: Int): Flow<List<MonthlyAggregate>> =
        transactionDao.getMonthlyAggregates(year.toString())
            .map { list -> list.map { it.toDomain() } }

    override fun getPendingSmsTransactions(): Flow<List<PendingSmsTransaction>> =
        pendingSmsDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insertPendingSmsTransaction(pending: PendingSmsTransaction) =
        pendingSmsDao.insert(pending.toEntity())

    override suspend fun deletePendingSmsTransaction(id: String) =
        pendingSmsDao.deleteById(id)

    private fun daysInMonth(month: Int, year: Int): Int =
        java.time.YearMonth.of(year, month).lengthOfMonth()
}
