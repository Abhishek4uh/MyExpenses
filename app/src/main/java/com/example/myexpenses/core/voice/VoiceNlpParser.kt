package com.example.myexpenses.core.voice

import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.TransactionType
import java.time.LocalDateTime

// ─── Parsed Result ────────────────────────────────────────────────────────────

data class ParsedVoiceIntent(
    val amount: Double?,
    val type: TransactionType?,
    val category: ExpenseCategory?,
    val note: String?,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val confidence: Float  // 0f–1f; show edit UI if < 0.7
)

// ─── NLP Parser ───────────────────────────────────────────────────────────────
// Pure Kotlin — no Android dependencies. Fully unit-testable.

object VoiceNLPParser {

    // ── Expense signals ───────────────────────────────────────────────────────
    private val EXPENSE_KEYWORDS = setOf(
        "spent", "spend", "paid", "pay", "bought", "buy", "purchased",
        "debited", "charged", "withdrew", "withdrawal", "expense",
        "kharcha", "diya", "liya"  // Hinglish
    )

    // ── Income signals ────────────────────────────────────────────────────────
    private val INCOME_KEYWORDS = setOf(
        "received", "got", "earned", "credited", "salary", "income","credit","receive",
        "payment received", "mila", "aaya"  // Hinglish
    )

    // ── Amount patterns ───────────────────────────────────────────────────────
    private val AMOUNT_REGEX = Regex(
        """(?:₹|rs\.?|inr)?\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // ── Category keyword map ──────────────────────────────────────────────────
    private val CATEGORY_KEYWORDS: Map<ExpenseCategory, Set<String>> = mapOf(
        ExpenseCategory.RENT to setOf("rent", "house rent", "pg", "accommodation", "kiraya"),
        ExpenseCategory.COMMUTE to setOf(
            "commute", "travel", "metro", "bus", "auto", "cab", "uber",
            "ola", "petrol", "fuel", "train", "ticket", "transport"
        ),
        ExpenseCategory.DINEIN to setOf(
            "dine in", "dinein", "dine-in", "lunch", "dinner", "breakfast",
            "restaurant", "meal", "khana", "cafe", "diner"
        ),
        ExpenseCategory.ONLINE_FOOD to setOf(
            "swiggy", "zomato", "online food", "delivery", "order", "ordered food"
        ),
        ExpenseCategory.GROCERY to setOf(
            "grocery", "groceries", "vegetables", "sabzi", "kirana",
            "bigbasket", "blinkit", "zepto", "grofers", "supermarket"
        ),
        ExpenseCategory.SHOPPING to setOf(
            "shopping", "clothes", "kapde", "fashion", "amazon order",
            "flipkart", "myntra", "ajio", "shoes", "accessories"
        ),
        ExpenseCategory.UTILITIES to setOf(
            // Home goods / utensils / small appliances — not recurring bills.
            "utility", "utensil", "utensils", "appliance", "appliances",
            "kadhai", "tawa", "cooker", "pressure cooker", "mixer", "blender",
            "iron", "fan", "kettle", "toaster", "kitchenware", "kitchen",
            "glass", "glasses", "plate", "plates", "bowl", "cutlery",
            "home goods", "household"
        ),
        ExpenseCategory.INSURANCE to setOf(
            "insurance", "policy", "premium", "lic", "health insurance", "term plan"
        ),
        ExpenseCategory.HEALTHCARE to setOf(
            "medicine", "doctor", "hospital", "pharmacy", "medical",
            "health", "clinic", "dava", "dawai", "checkup"
        ),
        ExpenseCategory.INVESTMENT to setOf(
            "investment", "invest", "saving", "savings", "emi", "loan",
            "mutual fund", "sip", "fd", "bachat", "stocks", "shares", "ppf"
        ),
        ExpenseCategory.PERSONALCARE to setOf(
            "salon", "haircut", "grooming", "spa", "beauty",
            "personal", "personal care", "hygiene", "skincare"
        ),
        ExpenseCategory.ENTERTAINMENT to setOf(
            "movie", "netflix", "spotify", "amazon prime", "hotstar",
            "concert", "game", "entertainment", "outing", "party",
            "subscription", "ott"
        ),
        ExpenseCategory.SALARY to setOf("salary", "stipend", "paycheck", "tanchha"),
        ExpenseCategory.FREELANCE to setOf("freelance", "project payment", "client payment"),
        ExpenseCategory.POCKET_MONEY to setOf("pocket money", "pocketmoney", "kharcha", "allowance"),
        ExpenseCategory.OTHER_INCOME to setOf("cashback", "refund", "bonus", "gift"),
        ExpenseCategory.MISCELLANEOUS to setOf("misc", "other", "random", "sundry")
    )

    // ── Date resolution ───────────────────────────────────────────────────────
    private val TODAY_KEYWORDS = setOf("today", "aaj", "abhi", "now", "just now")
    private val YESTERDAY_KEYWORDS = setOf("yesterday", "kal", "last night")
    private val DAY_KEYWORDS = mapOf(
        "monday" to 1, "tuesday" to 2, "wednesday" to 3,
        "thursday" to 4, "friday" to 5, "saturday" to 6, "sunday" to 7
    )

    // ── Word-to-number (basic Indian English) ─────────────────────────────────
    private val WORD_NUMBERS = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19, "twenty" to 20, "thirty" to 30,
        "forty" to 40, "fifty" to 50, "sixty" to 60, "seventy" to 70,
        "eighty" to 80, "ninety" to 90, "hundred" to 100, "thousand" to 1000,
        "lakh" to 100_000, "lac" to 100_000
    )

    // ─── Main Parse Entry Point ───────────────────────────────────────────────

    fun parse(rawInput: String): ParsedVoiceIntent {
        val input = rawInput.trim().lowercase()

        val amount = extractAmount(input)
        val type = extractType(input)
        val category = extractCategory(input)
        val dateTime = extractDateTime(input)

        val confidence = listOf(amount, type, category).count { it != null } / 3f

        return ParsedVoiceIntent(
            amount = amount,
            type = type ?: defaultTypeForCategory(category),
            category = category ?: ExpenseCategory.MISCELLANEOUS,
            note = buildNote(input),
            dateTime = dateTime,
            confidence = confidence
        )
    }

    // ─── Extraction Helpers ───────────────────────────────────────────────────

    private fun extractAmount(input: String): Double? {
        AMOUNT_REGEX.find(input)?.groupValues?.get(1)?.let { raw ->
            val cleaned = raw.replace(",", "")
            return cleaned.toDoubleOrNull()
        }
        return parseWordNumber(input)
    }

    private fun parseWordNumber(input: String): Double? {
        var total = 0
        var current = 0
        val words = input.split(" ")
        var found = false

        for (word in words) {
            val num = WORD_NUMBERS[word] ?: continue
            found = true
            when {
                num >= 100_000 -> { total += current * num; current = 0 }
                num >= 1_000 -> { total += current * num; current = 0 }
                num == 100 -> current *= num
                else -> current += num
            }
        }
        total += current
        return if (found && total > 0) total.toDouble() else null
    }

    private fun extractType(input: String): TransactionType? = when {
        EXPENSE_KEYWORDS.any { input.contains(it) } -> TransactionType.EXPENSE
        INCOME_KEYWORDS.any { input.contains(it) } -> TransactionType.INCOME
        else -> null
    }

    private fun defaultTypeForCategory(category: ExpenseCategory?): TransactionType =
        when (category) {
            ExpenseCategory.SALARY,
            ExpenseCategory.FREELANCE,
            ExpenseCategory.POCKET_MONEY,
            ExpenseCategory.OTHER_INCOME -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

    private fun extractCategory(input: String): ExpenseCategory? {
        var bestMatch: ExpenseCategory? = null
        var bestScore = 0

        CATEGORY_KEYWORDS.forEach { (category, keywords) ->
            val score = keywords.count { keyword -> input.contains(keyword) }
            if (score > bestScore) {
                bestScore = score
                bestMatch = category
            }
        }

        return bestMatch
    }

    private fun extractDateTime(input: String): LocalDateTime {
        val now = LocalDateTime.now()
        return when {
            TODAY_KEYWORDS.any { input.contains(it) } -> now
            YESTERDAY_KEYWORDS.any { input.contains(it) } -> now.minusDays(1)
            else -> {
                DAY_KEYWORDS.entries.firstOrNull { (day, _) -> input.contains(day) }
                    ?.let { (_, dayOfWeek) ->
                        val todayDow = now.dayOfWeek.value
                        val daysBack = ((todayDow - dayOfWeek + 7) % 7).let {
                            if (it == 0) 7 else it
                        }
                        now.minusDays(daysBack.toLong())
                    } ?: now
            }
        }
    }

    private fun buildNote(input: String): String {
        return input.replaceFirstChar { it.uppercase() }.take(100)
    }
}
