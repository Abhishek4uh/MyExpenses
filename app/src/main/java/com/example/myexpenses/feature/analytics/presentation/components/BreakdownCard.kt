package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.presentation.GroupAmount

/**
 * Donut chart + legend. Each group renders as a stroke arc starting at 12
 * o'clock (-90°). Arcs animate in with stroke-sweep over 600ms with 50ms
 * stagger between segments. Legend rows are tappable → drill-down to
 * CategoryDetail.
 */
@Composable
fun BreakdownCard(
    breakdown: List<GroupAmount>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nonZero = breakdown.filter { it.amount > 0 }
    val total = nonZero.sumOf { it.amount }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgElev1)
            .border(1.dp, BorderDefault, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        "Where it went",
                        fontSize = 12.sp,
                        fontFamily = InterFamily,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "By category",
                        fontSize = 16.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
                Text(
                    "Tap to drill down",
                    fontSize = 11.sp,
                    fontFamily = InterFamily,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(18.dp))

            if (total <= 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add an expense to see this",
                            fontSize = 12.sp,
                            fontFamily = InterFamily,
                            color = TextMuted,
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Donut(
                        breakdown = nonZero,
                        total = total,
                        modifier = Modifier.size(140.dp),
                    )
                    Spacer(Modifier.size(18.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        nonZero.take(5).forEach { entry ->
                            LegendRow(entry, onCategoryClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Donut(
    breakdown: List<GroupAmount>,
    total: Long,
    modifier: Modifier = Modifier,
) {
    // Animate the global sweep from 0 → 1 on first composition / data change.
    var triggered by remember(breakdown) { mutableStateOf(false) }
    LaunchedEffect(breakdown) { triggered = true }
    val sweepProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "donut_sweep",
    )
    val fadeAlpha by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "donut_alpha",
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 18.dp.toPx()
            val r = (size.minDimension - stroke) / 2f
            val topLeft = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
            val arcSize = Size(r * 2, r * 2)

            // Track behind the arcs
            drawArc(
                color = Color(0xFF161616),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )

            // Each group as a stroke arc
            var angle = -90f
            breakdown.forEach { entry ->
                val full = entry.pct * 360f
                val sweep = full * sweepProgress
                if (sweep > 0f) {
                    drawArc(
                        color = entry.group.tone.fg,
                        startAngle = angle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke),
                    )
                }
                angle += full
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp).run {
                // alpha animation on center label
                this
            },
        ) {
            Text(
                "TOTAL",
                fontSize = 10.sp,
                fontFamily = InterFamily,
                color = TextMuted,
                letterSpacing = 0.8.sp,
            )
            Text(
                "₹${total.formatCompact()}",
                fontSize = 16.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary.copy(alpha = fadeAlpha),
            )
        }
    }
}

@Composable
private fun LegendRow(entry: GroupAmount, onClick: (String) -> Unit) {
    val pct = (entry.pct * 100).toInt()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick(entry.group.id) }
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(entry.group.tone.fg)
        )
        Text(
            entry.group.displayName,
            fontSize = 12.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            "$pct%",
            fontSize = 11.sp,
            fontFamily = InterFamily,
            color = TextMuted,
        )
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
