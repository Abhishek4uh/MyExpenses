package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * V3 Breakdown card:
 *  - Donut centered at top (tappable slices → highlight + scroll to category)
 *  - Full category list below with: color dot | name | ₹amount | pct | mini bar
 *
 * Top 5 non-zero groups shown; the rest collapse into an "Others" row.
 */
@Composable
fun BreakdownCard(
    breakdown: List<GroupAmount>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nonZero = breakdown.filter { it.amount > 0 }
    val total   = nonZero.sumOf { it.amount }

    // Top 5 + Others aggregation
    val top5    = nonZero.take(5)
    val others  = nonZero.drop(5)
    val othersAmount = others.sumOf { it.amount }
    val othersColor  = Color(0xFF555555)

    var selectedIndex by remember(breakdown) { mutableIntStateOf(-1) }

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
                verticalAlignment     = Alignment.Top,
            ) {
                Column {
                    Text(
                        "Where it went",
                        fontSize   = 12.sp,
                        fontFamily = InterFamily,
                        color      = TextTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Category breakdown",
                        fontSize   = 16.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                }
                Text(
                    "Tap to drill down",
                    fontSize   = 11.sp,
                    fontFamily = InterFamily,
                    color      = TextMuted,
                )
            }

            Spacer(Modifier.height(18.dp))

            if (total <= 0L) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add an expense to see this",
                            fontSize   = 12.sp,
                            fontFamily = InterFamily,
                            color      = TextMuted,
                        )
                    }
                }
            } else {
                // Donut centered
                Box(
                    modifier          = Modifier.fillMaxWidth(),
                    contentAlignment  = Alignment.Center,
                ) {
                    DonutChart(
                        items         = top5,
                        othersAmount  = othersAmount,
                        othersColor   = othersColor,
                        total         = total,
                        selectedIndex = selectedIndex,
                        onSliceTap    = { idx -> selectedIndex = if (selectedIndex == idx) -1 else idx },
                        modifier      = Modifier.size(180.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Category list
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    top5.forEachIndexed { i, entry ->
                        CategoryRow(
                            color      = entry.group.tone.fg,
                            name       = entry.group.displayName,
                            amount     = entry.amount,
                            pct        = entry.pct,
                            selected   = selectedIndex == i,
                            onClick    = {
                                selectedIndex = if (selectedIndex == i) -1 else i
                                onCategoryClick(entry.group.id)
                            },
                        )
                    }
                    if (othersAmount > 0L) {
                        val othersPct = if (total > 0) othersAmount.toFloat() / total else 0f
                        CategoryRow(
                            color    = othersColor,
                            name     = "Others",
                            amount   = othersAmount,
                            pct      = othersPct,
                            selected = false,
                            onClick  = {},
                        )
                    }
                }
            }
        }
    }
}

// ─── Donut ─────────────────────────────────────────────────────────────────────

@Composable
private fun DonutChart(
    items: List<GroupAmount>,
    othersAmount: Long,
    othersColor: Color,
    total: Long,
    selectedIndex: Int,
    onSliceTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var triggered by remember(items) { mutableStateOf(false) }
    LaunchedEffect(items) { triggered = true }
    val sweepProgress by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label         = "donut_sweep",
    )
    val fadeAlpha by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        label         = "donut_fade",
    )

    // Build arc list: top5 groups + optional others
    data class Arc(val color: Color, val pct: Float, val index: Int)
    val arcs = buildList {
        items.forEachIndexed { i, g -> add(Arc(g.group.tone.fg, g.pct, i)) }
        if (othersAmount > 0L && total > 0) {
            add(Arc(othersColor, othersAmount.toFloat() / total, -1))
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .pointerInput(arcs) {
                    detectTapGestures { tap ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = tap.x - cx
                        val dy = tap.y - cy
                        val dist = sqrt(dx * dx + dy * dy)
                        val strokePx = 22.dp.toPx()
                        val outerR   = minOf(size.width, size.height).toFloat() / 2f
                        val innerR   = outerR - strokePx
                        if (dist < innerR || dist > outerR) return@detectTapGestures
                        // Angle: atan2 in -π..π, shift so 0° = top (-90°)
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angle < 0) angle += 360f
                        var cum = 0f
                        arcs.forEach { arc ->
                            val sweep = arc.pct * 360f * sweepProgress
                            if (angle in cum..(cum + sweep)) {
                                if (arc.index >= 0) onSliceTap(arc.index)
                                return@detectTapGestures
                            }
                            cum += arc.pct * 360f * sweepProgress
                        }
                    }
                }
        ) {
            val stroke   = 22.dp.toPx()
            val r        = (size.minDimension - stroke) / 2f
            val topLeft  = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
            val arcSize  = Size(r * 2, r * 2)

            // Track
            drawArc(
                color       = Color(0xFF161616),
                startAngle  = 0f,
                sweepAngle  = 360f,
                useCenter   = false,
                topLeft     = topLeft,
                size        = arcSize,
                style       = Stroke(width = stroke),
            )

            var angle = -90f
            arcs.forEachIndexed { i, arc ->
                val full      = arc.pct * 360f
                val sweep     = full * sweepProgress
                val isSelected = arc.index == selectedIndex
                val arcStroke = if (isSelected) stroke * 1.2f else stroke
                val arcR      = if (isSelected) (size.minDimension - arcStroke) / 2f else r
                val arcTL     = if (isSelected)
                    Offset((size.width - arcR * 2) / 2f, (size.height - arcR * 2) / 2f)
                else topLeft
                val arcSz     = if (isSelected) Size(arcR * 2, arcR * 2) else arcSize

                if (sweep > 0f) {
                    drawArc(
                        color       = arc.color.copy(alpha = if (selectedIndex == -1 || isSelected) 1f else 0.4f),
                        startAngle  = angle,
                        sweepAngle  = sweep - if (arcs.size > 1) 1.5f else 0f,
                        useCenter   = false,
                        topLeft     = arcTL,
                        size        = arcSz,
                        style       = Stroke(width = arcStroke, cap = StrokeCap.Round),
                    )
                }
                angle += full
            }
        }

        // Center label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "TOTAL",
                fontSize      = 10.sp,
                fontFamily    = InterFamily,
                color         = TextMuted,
                letterSpacing = 0.8.sp,
            )
            Text(
                "₹${total.formatCompact()}",
                fontSize   = 18.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary.copy(alpha = fadeAlpha),
            )
        }
    }
}

// ─── Category row ──────────────────────────────────────────────────────────────

@Composable
private fun CategoryRow(
    color: Color,
    name: String,
    amount: Long,
    pct: Float,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgAlpha = if (selected) 0.06f else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = bgAlpha))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        // Name
        Text(
            name,
            fontSize   = 13.sp,
            fontFamily = InterFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) TextPrimary else TextPrimary.copy(alpha = 0.85f),
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.weight(1f),
        )

        // Amount
        Text(
            "₹${amount.formatCompact()}",
            fontSize   = 12.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color      = if (selected) color else TextPrimary,
        )

        // Pct
        val pctInt = (pct * 100).toInt()
        Text(
            "$pctInt%",
            fontSize   = 11.sp,
            fontFamily = InterFamily,
            color      = TextMuted,
            modifier   = Modifier.padding(start = 2.dp),
        )
    }

    // Mini progress bar
    val animated by animateFloatAsState(
        targetValue   = pct.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label         = "cat_bar_$name",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(BgElev3)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(2.dp)
                .background(color.copy(alpha = if (selected) 1f else 0.55f), RoundedCornerShape(1.dp))
        )
    }
}

// ─── Shared util ───────────────────────────────────────────────────────────────

private fun Long.formatCompact(): String = when {
    this >= 100_000 -> "%.1fL".format(this / 100_000.0)
    this >= 1_000   -> "%.1fK".format(this / 1_000.0)
    else            -> "%,d".format(this)
}
