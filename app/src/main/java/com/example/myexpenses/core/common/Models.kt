package com.example.myexpenses.core.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// ─── Transaction Type ───────────────────────────────────────────────────────

enum class TransactionType {
    INCOME, EXPENSE
}

// ─── Entry Source ───────────────────────────────────────────────────────────

enum class EntrySource {
    MANUAL,   //User typed it in
    VOICE,    //Parsed from speech
    SMS       //Auto-read from bank SMS
}

// ─── Category ───────────────────────────────────────────────────────────────

enum class ExpenseCategory(
    val displayName: String,
    val emoji: String,
    val colorHex: String){
    // ── Expense ──
    RENT("Rent", "🏠", "#FF6B6B"),
    COMMUTE("Commute", "🚇", "#4ECDC4"),
    DINEIN("Dine In", "🍱", "#FFE66D"),
    ONLINE_FOOD("Online Food", "📱", "#FF8C42"),
    GROCERY("Grocery", "🛒", "#A8DADC"),
    SHOPPING("Shopping", "🛍️", "#F4A261"),
    UTILITIES("Utility", "🍳", "#D7BD7F"),
    INSURANCE("Insurance", "🛡️", "#457B9D"),
    HEALTHCARE("Healthcare", "💊", "#E63946"),
    INVESTMENT("Investment", "📈", "#2DC653"),
    PERSONALCARE("Personal Care", "🧴", "#B5838D"),
    ENTERTAINMENT("Entertainment", "🎬", "#9B5DE5"),
    MISCELLANEOUS("Miscellaneous", "📦", "#8D99AE"),

    // ── Income ──
    SALARY("Salary", "💼", "#06D6A0"),
    FREELANCE("Freelance", "💻", "#118AB2"),
    POCKET_MONEY("Pocket Money", "💵", "#F2B23A"),
    OTHER_INCOME("Other Income", "🎁", "#73D2DE")
}

// ─── Core Transaction Model ──────────────────────────────────────────────────

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: ExpenseCategory,
    val note: String = "",
    val source: EntrySource = EntrySource.MANUAL,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val isConfirmed: Boolean = true  // false = pending SMS confirm
)

// ─── Dashboard Stats ──────────────────────────────────────────────────────────

data class DashboardStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double = totalIncome - totalExpense,
    val savingsRate: Float = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).toFloat() else 0f
)

data class CategoryBreakdown(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class DailyAggregate(
    val date: LocalDateTime,
    val income: Double,
    val expense: Double
)

data class MonthlyAggregate(
    val month: Int,
    val year: Int,
    val income: Double,
    val expense: Double
)

// ─── Pending SMS Transaction (needs user confirm) ────────────────────────────

data class PendingSmsTransaction(
    val id: String = UUID.randomUUID().toString(),
    val rawSmsBody: String,
    val parsedAmount: Double?,
    val parsedType: TransactionType?,
    val suggestedCategory: ExpenseCategory?,
    val senderName: String,
    val receivedAt: LocalDateTime = LocalDateTime.now()
)

// ─── Streak ──────────────────────────────────────────────────────────────────

enum class DayStatus { COMPLETED, MISSED, TODAY, FUTURE }

data class StreakData(
    val currentStreak: Int,
    val activeDays: Set<LocalDate>,
    val onboardingDate: LocalDate,
)

// ─── User Preferences ────────────────────────────────────────────────────────

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val isSmsReaderEnabled: Boolean = false,
    val isRemindersEnabled: Boolean = true,
    val currency: String = "₹",
    val onboardingComplete: Boolean = false,
    val name: String = "",
)
