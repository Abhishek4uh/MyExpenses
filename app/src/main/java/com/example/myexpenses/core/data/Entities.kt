package com.example.myexpenses.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: String,          // TransactionType.name
    val category: String,      // ExpenseCategory.name
    val note: String,
    val source: String,        // EntrySource.name
    val timestamp: Long,       // epochMilli from LocalDateTime
    val isConfirmed: Boolean = true
)

@Entity(tableName = "pending_sms")
data class PendingSmsEntity(
    @PrimaryKey val id: String,
    val rawSmsBody: String,
    val parsedAmount: Double?,
    val parsedType: String?,
    val suggestedCategory: String?,
    val senderName: String,
    val receivedAt: Long
)
