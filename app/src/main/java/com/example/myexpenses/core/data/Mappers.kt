package com.example.myexpenses.core.data

import com.example.myexpenses.core.common.CategoryBreakdown
import com.example.myexpenses.core.common.DailyAggregate
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.MonthlyAggregate
import com.example.myexpenses.core.common.PendingSmsTransaction
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ─── Transaction ──────────────────────────────────────────────────────────────

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    note = note,
    source = source.name,
    timestamp = dateTime.toEpochMilli(),
    isConfirmed = isConfirmed
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = parseCategory(category),
    note = note,
    source = EntrySource.valueOf(source),
    dateTime = timestamp.toLocalDateTime(),
    isConfirmed = isConfirmed
)

/**
 * Tolerant ExpenseCategory lookup that maps legacy enum names from older app
 * versions to their current equivalents. Existing DB rows reference these
 * legacy strings; without the alias map, [ExpenseCategory.valueOf] would
 * throw IllegalArgumentException and the app would crash on launch.
 */
private fun parseCategory(stored: String): ExpenseCategory {
    val normalized = when (stored) {
        "FOOD" -> "DINEIN"
        "SAVING_DEBT" -> "INVESTMENT"
        "PERSONAL" -> "PERSONALCARE"
        else -> stored
    }
    return runCatching { ExpenseCategory.valueOf(normalized) }
        .getOrDefault(ExpenseCategory.MISCELLANEOUS)
}

// ─── Pending SMS ──────────────────────────────────────────────────────────────

fun PendingSmsTransaction.toEntity(): PendingSmsEntity = PendingSmsEntity(
    id = id,
    rawSmsBody = rawSmsBody,
    parsedAmount = parsedAmount,
    parsedType = parsedType?.name,
    suggestedCategory = suggestedCategory?.name,
    senderName = senderName,
    receivedAt = receivedAt.toEpochMilli()
)

fun PendingSmsEntity.toDomain(): PendingSmsTransaction = PendingSmsTransaction(
    id = id,
    rawSmsBody = rawSmsBody,
    parsedAmount = parsedAmount,
    parsedType = parsedType?.let { TransactionType.valueOf(it) },
    suggestedCategory = suggestedCategory?.let { parseCategory(it) },
    senderName = senderName,
    receivedAt = receivedAt.toLocalDateTime()
)

// ─── Aggregate Mappers ────────────────────────────────────────────────────────

fun DailyTotal.toDomain(): DailyAggregate = DailyAggregate(
    date = dayTimestamp.toLocalDateTime(),
    income = incomeTotal,
    expense = expenseTotal
)

fun MonthlyTotal.toDomain(): MonthlyAggregate = MonthlyAggregate(
    month = month,
    year = year,
    income = incomeTotal,
    expense = expenseTotal
)

fun List<CategoryTotal>.toDomainBreakdown(type: TransactionType): List<CategoryBreakdown> {
    val grandTotal = sumOf { it.total }.takeIf { it > 0 } ?: 1.0
    return map { raw ->
        CategoryBreakdown(
            category = parseCategory(raw.category),
            amount = raw.total,
            percentage = (raw.total / grandTotal * 100).toFloat(),
            transactionCount = raw.txCount
        )
    }
}

// ─── Time Helpers ─────────────────────────────────────────────────────────────

private val defaultZone: ZoneId = ZoneId.systemDefault()

fun LocalDateTime.toEpochMilli(): Long =
    atZone(defaultZone).toInstant().toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(defaultZone).toLocalDateTime()
