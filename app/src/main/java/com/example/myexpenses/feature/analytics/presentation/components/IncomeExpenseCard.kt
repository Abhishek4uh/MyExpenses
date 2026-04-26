package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.Success
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.presentation.IncomeExpense

/**
 * Two horizontal bars showing income vs expense, with a savings/overspend
 * sub-title and a green pill on the right when savings is positive.
 *
 * Bar widths animate over 400ms with FastOutSlowInEasing on data change.
 */
@Composable
fun IncomeExpenseCard(
    data: IncomeExpense,
    modifier: Modifier = Modifier,
) {
    val maxValue = maxOf(data.income, data.expense, 1L).toFloat()
    val savingsPct: Int? = if (data.income > 0) {
        (((data.income - data.expense).toFloat() / data.income) * 100).toInt()
    } else null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgElev1)
            .border(1.dp, BorderDefault, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        "Income vs Expense",
                        fontSize = 12.sp,
                        fontFamily = InterFamily,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    val subtitle = when {
                        savingsPct == null -> "Add income to compare"
                        savingsPct >= 0 -> "Saving $savingsPct%"
                        else -> "Overspending ${-savingsPct}%"
                    }
                    Text(
                        subtitle,
                        fontSize = 16.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
                if (savingsPct != null && savingsPct > 0) {
                    val saved = data.income - data.expense
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Success.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "+₹${saved.formatCompact()}",
                            fontSize = 12.sp,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Success,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            BarRow(label = "Income", value = data.income, fraction = data.income / maxValue, color = Success)
            Spacer(Modifier.height(14.dp))
            BarRow(label = "Expense", value = data.expense, fraction = data.expense / maxValue, color = Danger)
        }
    }
}

@Composable
private fun BarRow(label: String, value: Long, fraction: Float, color: androidx.compose.ui.graphics.Color) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "bar_width_$label",
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                "₹${"%,d".format(value)}",
                fontSize = 12.sp,
                fontFamily = InterFamily,
                color = TextMuted,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BgElev3)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

private fun Long.formatCompact(): String = when {
    this >= 100_000 -> {
        val l = this / 100_000.0
        if (l == l.toInt().toDouble()) "${l.toInt()}L" else "%.1fL".format(l)
    }
    this >= 1_000 -> {
        val k = this / 1_000.0
        if (k == k.toInt().toDouble()) "${k.toInt()}K" else "%.1fK".format(k)
    }
    else -> "%,d".format(this)
}
