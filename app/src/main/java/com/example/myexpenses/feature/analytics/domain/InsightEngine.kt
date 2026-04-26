package com.example.myexpenses.feature.analytics.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.ui.graphics.vector.ImageVector

enum class InsightTone { WARN, GOOD, INFO }

data class Insight(
    val tone: InsightTone,
    val icon: ImageVector,
    val title: String,
    val body: String,
)

/**
 * Aggregated stats for one period — fed into [InsightEngine.generate].
 * All amounts are integer rupees (rounded for display).
 */
data class PeriodStats(
    val totalExpense: Long,
    val totalIncome: Long,
    val transactionCount: Int,
    val daysInRange: Int,
    val byGroup: Map<CategoryGroup, Long>,
)

/**
 * Generates the 2 most actionable observations for the current period.
 *
 * Rules in priority order — first 2 that fire, render. If fewer than 2
 * fire, the last fallback ("Avg daily spend") is always emitted.
 *
 * 1. WARN — any group up ≥ 25% vs previous period
 * 2. WARN — total up ≥ 15% vs previous period
 * 3. GOOD — income > expense → "You saved ₹X"
 * 4. GOOD — any group down ≥ 15%
 * 5. INFO — any group ≥ 50% of total expenses
 * 6. INFO — fallback "Avg daily spend"
 *
 * Returns empty list if [current.transactionCount] < 5 (per spec section 10:
 * "hide section entirely when < 5 transactions in range").
 */
object InsightEngine {

    fun generate(
        rangeLabel: String,         // e.g. "this month", "this week"
        current: PeriodStats,
        previous: PeriodStats,
    ): List<Insight> {
        if (current.transactionCount < 5) return emptyList()

        val candidates = buildList {
            // Rule 1 — group sharply up
            current.byGroup.entries
                .mapNotNull { (group, amount) ->
                    val prev = previous.byGroup[group] ?: 0L
                    if (prev <= 0L) return@mapNotNull null
                    val pct = ((amount - prev) * 100f / prev)
                    if (pct >= 25f) Triple(group, amount, pct) else null
                }
                .maxByOrNull { it.third }
                ?.let { (group, _, pct) ->
                    add(Insight(
                        tone = InsightTone.WARN,
                        icon = Icons.Outlined.ArrowUpward,
                        title = "${group.displayName} up ${pct.toInt()}%",
                        body = "₹${(current.byGroup[group]!! - previous.byGroup[group]!!).abs().formatRupees()} more than last period."
                    ))
                }

            // Rule 2 — total sharply up
            if (previous.totalExpense > 0) {
                val pct = ((current.totalExpense - previous.totalExpense) * 100f / previous.totalExpense)
                if (pct >= 15f) {
                    add(Insight(
                        tone = InsightTone.WARN,
                        icon = Icons.Outlined.ArrowUpward,
                        title = "Spending up ${pct.toInt()}% $rangeLabel",
                        body = "₹${(current.totalExpense - previous.totalExpense).formatRupees()} more than the previous period."
                    ))
                }
            }

            // Rule 3 — saving (income > expense)
            if (current.totalIncome > current.totalExpense && current.totalIncome > 0) {
                val saved = current.totalIncome - current.totalExpense
                val ratio = if (current.totalExpense > 0) current.totalIncome.toFloat() / current.totalExpense else 0f
                add(Insight(
                    tone = InsightTone.GOOD,
                    icon = Icons.Outlined.Savings,
                    title = "You saved ₹${saved.formatRupees()} $rangeLabel",
                    body = if (ratio > 0)
                        "Income outpaced spending by ${"%.1f".format(ratio)}×. On track for your savings goal."
                    else
                        "Income exceeded spending. Keep it up."
                ))
            }

            // Rule 4 — group sharply down
            current.byGroup.entries
                .mapNotNull { (group, amount) ->
                    val prev = previous.byGroup[group] ?: 0L
                    if (prev <= 0L) return@mapNotNull null
                    val pct = ((amount - prev) * 100f / prev)
                    if (pct <= -15f) Triple(group, amount, pct) else null
                }
                .minByOrNull { it.third }
                ?.let { (group, _, pct) ->
                    add(Insight(
                        tone = InsightTone.GOOD,
                        icon = Icons.Outlined.TrendingDown,
                        title = "${group.displayName} down ${(-pct).toInt()}%",
                        body = "₹${(previous.byGroup[group]!! - current.byGroup[group]!!).abs().formatRupees()} less than the previous period."
                    ))
                }

            // Rule 5 — single group dominates
            if (current.totalExpense > 0) {
                current.byGroup.entries
                    .firstOrNull { (_, amount) -> amount * 100f / current.totalExpense >= 50f }
                    ?.let { (group, amount) ->
                        val pct = (amount * 100f / current.totalExpense).toInt()
                        add(Insight(
                            tone = InsightTone.INFO,
                            icon = Icons.Outlined.AutoAwesome,
                            title = "${group.displayName} is $pct% of expenses",
                            body = "₹${amount.formatRupees()} of your ₹${current.totalExpense.formatRupees()} total spend $rangeLabel."
                        ))
                    }
            }

            // Rule 6 — always-available fallback
            val avgDaily = if (current.daysInRange > 0)
                current.totalExpense / current.daysInRange else 0L
            add(Insight(
                tone = InsightTone.INFO,
                icon = Icons.Outlined.AutoAwesome,
                title = "Avg daily spend: ₹${avgDaily.formatRupees()}",
                body = "Across ${current.daysInRange} day${if (current.daysInRange == 1) "" else "s"} in this period."
            ))
        }

        return candidates.take(2)
    }

    private fun Long.formatRupees(): String = "%,d".format(this)
    private fun Long.abs(): Long = if (this < 0L) -this else this
}
