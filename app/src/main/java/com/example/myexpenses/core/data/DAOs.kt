package com.example.myexpenses.core.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao {

    // ─── Writes ───────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    // ─── Reads ────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE isConfirmed = 1 ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        AND isConfirmed = 1
        ORDER BY timestamp DESC
    """)
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    // ─── Aggregates (used by DashboardStats) ─────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0.0) as income,
               COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0.0) as expense
        FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        AND isConfirmed = 1
    """)
    fun getIncomeExpenseTotals(startMs: Long, endMs: Long): Flow<IncomeExpenseTotal>

    // ─── Category Breakdown ───────────────────────────────────────────────────

    @Query("""
        SELECT category,
               SUM(amount) as total,
               COUNT(*) as txCount
        FROM transactions
        WHERE type = :type
        AND timestamp BETWEEN :startMs AND :endMs
        AND isConfirmed = 1
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategoryTotals(
        type: String,
        startMs: Long,
        endMs: Long
    ): Flow<List<CategoryTotal>>

    // ─── Daily Aggregates (for bar charts) ────────────────────────────────────
    // Groups by calendar day using SQLite's date() function on unix seconds

    @Query("""
        SELECT
            MIN(timestamp) as dayTimestamp,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as incomeTotal,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as expenseTotal
        FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        AND isConfirmed = 1
        GROUP BY date(timestamp / 1000, 'unixepoch', 'localtime')
        ORDER BY dayTimestamp ASC
    """)
    fun getDailyAggregates(startMs: Long, endMs: Long): Flow<List<DailyTotal>>

    // ─── Streak — distinct calendar days with at least one confirmed transaction ─

    @Query("""
        SELECT DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime') as epochDay
        FROM transactions
        WHERE isConfirmed = 1
    """)
    fun getDistinctActiveDays(): Flow<List<String>>

    // ─── Monthly Aggregates (for yearly chart) ────────────────────────────────

    @Query("""
        SELECT
            CAST(strftime('%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) AS INTEGER) as month,
            CAST(strftime('%Y', datetime(timestamp / 1000, 'unixepoch', 'localtime')) AS INTEGER) as year,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as incomeTotal,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as expenseTotal
        FROM transactions
        WHERE strftime('%Y', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = :year
        AND isConfirmed = 1
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyAggregates(year: String): Flow<List<MonthlyTotal>>
}

// ─── Pending SMS ──────────────────────────────────────────────────────────────

@Dao
interface PendingSmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingSmsEntity)

    @Query("SELECT * FROM pending_sms ORDER BY receivedAt DESC")
    fun getAll(): Flow<List<PendingSmsEntity>>

    @Query("DELETE FROM pending_sms WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ─── Query Result POJOs ───────────────────────────────────────────────────────

data class IncomeExpenseTotal(
    val income: Double,
    val expense: Double
)

data class CategoryTotal(
    val category: String,
    val total: Double,
    val txCount: Int
)

data class DailyTotal(
    val dayTimestamp: Long,
    val incomeTotal: Double,
    val expenseTotal: Double
)

data class MonthlyTotal(
    val month: Int,
    val year: Int,
    val incomeTotal: Double,
    val expenseTotal: Double
)
