package com.example.myexpenses.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        PendingSmsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun pendingSmsDao(): PendingSmsDao

    companion object {
        const val DATABASE_NAME = "expense_manager.db"
    }
}
