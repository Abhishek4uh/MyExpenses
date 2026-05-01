package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev2
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.Success
import com.example.myexpenses.core.ui.theme.TextFaint
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.presentation.DayBarData
import androidx.compose.foundation.Canvas
import kotlin.math.roundToInt

/**
 * Grouped bar chart for the Week view.
 *
 * Each day renders two bars side-by-side: income (green) on the left,
 * expense (red) on the right. Bars animate up from zero on data change.
 * Tap a bar group to see a tooltip with exact amounts above it.
 */
@Composable
fun GroupedBarChart(
    bars: List<DayBarData>,
    modifier: Modifier = Modifier,
) {
    var triggered by remember(bars) { mutableStateOf(false) }
    LaunchedEffect(bars) { triggered = true }
    val progress by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label         = "bar_progress",
    )

    var tooltipIndex by remember(bars) { mutableIntStateOf(-1) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgElev1)
            .border(1.dp, BorderDefault, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Card header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Income vs Expense",
                        fontSize   = 12.sp,
                        fontFamily = InterFamily,
                        color      = TextTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "This week",
                        fontSize   = 16.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                }
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot(Success, "Income")
                    LegendDot(Danger, "Expense")
                }
            }

            Spacer(Modifier.height(16.dp))

            val maxValue = bars.maxOfOrNull { maxOf(it.income, it.expense) }?.toFloat()
                ?.coerceAtLeast(1f) ?: 1f

            // BoxWithConstraints gives us pixel width for bar + tooltip positioning.
            // Canvas and tooltip are stacked inside (Box semantics).
            // X-axis labels live outside in the Column so they don't overlap the canvas.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val chartWidthPx = with(density) { maxWidth.toPx() }
                val groupCount   = bars.size.coerceAtLeast(1)
                val groupWidthPx = chartWidthPx / groupCount
                val barWidthPx   = groupWidthPx * 0.28f
                val gapPx        = groupWidthPx * 0.05f
                val cornerPx     = with(density) { 3.dp.toPx() }
                val barAreaH     = with(density) { 140.dp.toPx() }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .pointerInput(bars) {
                            detectTapGestures { tap ->
                                val idx = (tap.x / groupWidthPx).toInt().coerceIn(0, groupCount - 1)
                                tooltipIndex = if (tooltipIndex == idx) -1 else idx
                            }
                        }
                ) {
                    bars.forEachIndexed { i, bar ->
                        val groupCenterX = groupWidthPx * i + groupWidthPx / 2f
                        drawGroupedBars(
                            incomeX  = groupCenterX - barWidthPx - gapPx / 2,
                            expenseX = groupCenterX + gapPx / 2,
                            barW      = barWidthPx,
                            cornerR   = cornerPx,
                            barAreaH  = barAreaH,
                            maxValue  = maxValue,
                            income    = bar.income,
                            expense   = bar.expense,
                            progress  = progress,
                            highlight = tooltipIndex == i,
                        )
                    }
                }

                // Tooltip floats above the bars (z-ordered on top by Box stacking)
                if (tooltipIndex in bars.indices) {
                    val bar      = bars[tooltipIndex]
                    val groupCX  = groupWidthPx * tooltipIndex + groupWidthPx / 2f
                    val tooltipW = with(density) { 100.dp.toPx() }
                    val clampedX = (groupCX - tooltipW / 2f).coerceIn(0f, chartWidthPx - tooltipW)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(clampedX.roundToInt(), 4) }
                            .width(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgElev2)
                            .border(1.dp, BorderDefault, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            TooltipRow("Income", bar.income, Success)
                            Spacer(Modifier.height(2.dp))
                            TooltipRow("Exp", bar.expense, Danger)
                        }
                    }
                }
            }

            // X-axis: each Text gets weight(1f) so slot width = groupWidthPx exactly.
            // TextAlign.Center keeps the label centred within its slot → aligned with bars.
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                bars.forEach { bar ->
                    Text(
                        bar.label,
                        fontSize   = 10.sp,
                        fontFamily = InterFamily,
                        color      = TextFaint,
                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGroupedBars(
    incomeX: Float,
    expenseX: Float,
    barW: Float,
    cornerR: Float,
    barAreaH: Float,
    maxValue: Float,
    income: Long,
    expense: Long,
    progress: Float,
    highlight: Boolean,
) {
    val incomeH  = (income  / maxValue) * barAreaH * progress
    val expenseH = (expense / maxValue) * barAreaH * progress
    val alpha    = if (highlight) 1f else 0.85f

    if (incomeH > 0f) {
        drawRoundRect(
            color        = Success.copy(alpha = alpha),
            topLeft      = Offset(incomeX, barAreaH - incomeH),
            size         = Size(barW, incomeH),
            cornerRadius = CornerRadius(cornerR),
        )
    }
    if (expenseH > 0f) {
        drawRoundRect(
            color        = Danger.copy(alpha = alpha),
            topLeft      = Offset(expenseX, barAreaH - expenseH),
            size         = Size(barW, expenseH),
            cornerRadius = CornerRadius(cornerR),
        )
    }
    // Highlight ring around selected group
    if (highlight) {
        val ringL = incomeX - 3f
        val ringT = 0f
        val ringW = barW * 2 + (expenseX - incomeX - barW) + 6f
        drawRoundRect(
            color        = Color.White.copy(alpha = 0.08f),
            topLeft      = Offset(ringL, ringT),
            size         = Size(ringW, barAreaH),
            cornerRadius = CornerRadius(cornerR + 2f),
        )
    }
}

@Composable
private fun TooltipRow(label: String, value: Long, color: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 10.sp, fontFamily = InterFamily, color = TextMuted)
        Text(
            "₹${value.formatCompact()}",
            fontSize   = 10.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color      = color,
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 11.sp, fontFamily = InterFamily, color = TextMuted)
    }
}

private fun Long.formatCompact(): String = when {
    this >= 100_000 -> {
        val l = this / 100_000.0
        if (l == l.toInt().toDouble()) "${l.toInt()}L" else "%.1fL".format(l)
    }
    this >= 1_000   -> {
        val k = this / 1_000.0
        if (k == k.toInt().toDouble()) "${k.toInt()}K" else "%.1fK".format(k)
    }
    else            -> "%,d".format(this)
}
