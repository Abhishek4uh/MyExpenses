package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextFaint
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.presentation.TrendSeries
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Smooth area trend chart with header (eyebrow + total + signed delta pill).
 *
 * The path is built via Catmull-Rom interpolation between data points and
 * converted into cubic-bezier segments that Compose's [Path] can render —
 * this gives a soft curve instead of straight line segments.
 *
 * Anim: the entire chart fades + scales in on data change (via a single
 * "progress" Animatable that scales the Y values from 0 → real, and ramps
 * area alpha 0 → 0.32). Path morphing per-point is overkill since the X
 * domain typically also changes when the period switches.
 */
@Composable
fun TrendCard(
    trend: TrendSeries,
    totalLabel: String,
    delta: Float,                  // signed pct (-0.08 = -8%)
    accent: Color = Accents.Amber,
    eyebrow: String = "Spending trend",
    modifier: Modifier = Modifier,
) {
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
                        eyebrow,
                        fontSize = 12.sp,
                        fontFamily = InterFamily,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        totalLabel,
                        fontSize = 22.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp,
                    )
                }
                if (delta != 0f) {
                    DeltaPill(delta, accent)
                }
            }

            Spacer(Modifier.height(14.dp))
            TrendChart(trend, accent)

            Spacer(Modifier.height(8.dp))
            XAxisRow(trend)
        }
    }
}

@Composable
private fun DeltaPill(delta: Float, accent: Color) {
    val pct = (abs(delta) * 100).roundToInt()
    val isUp = delta > 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(accent.copy(alpha = 0.13f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(
            if (isUp) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.padding(end = 2.dp).then(Modifier),
        )
        Text(
            "$pct% vs prev",
            fontSize = 12.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
private fun TrendChart(trend: TrendSeries, accent: Color) {
    var triggered by remember(trend) { mutableStateOf(false) }
    LaunchedEffect(trend) { triggered = true }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "trend_progress",
    )
    val fillAlpha by animateFloatAsState(
        targetValue = if (triggered) 0.32f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "trend_fill_alpha",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)){
        if (trend.values.isEmpty() || trend.values.all { it == 0L }) {
            // Empty/flat state — single dashed line at baseline
            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val mid = size.height / 2f
                drawLine(
                    color = Color(0xFF1A1A1A),
                    start = Offset(0f, mid),
                    end = Offset(size.width, mid),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx())),
                )
            }
            return@BoxWithConstraints
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val padding = 10.dp.toPx()
            val w = size.width - padding * 2
            val h = size.height - padding * 2
            val maxV = trend.values.max().toFloat().coerceAtLeast(1f)

            // Compute points (X evenly distributed, Y scaled by progress)
            val pts: List<Offset> = trend.values.mapIndexed { i, v ->
                val x = padding + (if (trend.values.size == 1) w / 2f
                    else w * i / (trend.values.size - 1).toFloat())
                val y = padding + h - (v / maxV) * h * progress
                Offset(x, y)
            }

            // ── Dashed grid lines at 25 / 50 / 75 ──
            val gridDash = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx()))
            listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                val y = padding + h * frac
                drawLine(
                    color = Color(0xFF1A1A1A),
                    start = Offset(padding, y),
                    end = Offset(padding + w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = gridDash,
                )
            }

            // ── Smooth Catmull-Rom path ──
            val linePath = catmullRomPath(pts)
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(pts.last().x, padding + h)
                lineTo(pts.first().x, padding + h)
                close()
            }

            // Area fill (vertical gradient)
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accent.copy(alpha = fillAlpha), Color.Transparent),
                    startY = padding,
                    endY = padding + h,
                ),
            )

            // Line stroke
            drawPath(
                path = linePath,
                color = accent,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            // Peak dot
            val peakIdx = trend.values.indices.maxByOrNull { trend.values[it] } ?: -1
            if (peakIdx >= 0 && peakIdx < pts.size && progress > 0.5f) {
                val peak = pts[peakIdx]
                drawCircle(color = accent.copy(alpha = 0.2f), radius = 6.dp.toPx(), center = peak)
                drawCircle(color = accent, radius = 3.dp.toPx(), center = peak)
            }
        }
    }
}

@Composable
private fun XAxisRow(trend: TrendSeries) {
    if (trend.labels.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        trend.xTickIndices.forEach { idx ->
            val label = trend.labels.getOrNull(idx) ?: return@forEach
            Text(
                label,
                fontSize = 10.sp,
                fontFamily = InterFamily,
                color = TextFaint,
            )
        }
    }
}

/**
 * Catmull-Rom-to-Bezier conversion. Produces a smooth Path passing through
 * every input point. Tension 0.5 (uniform Catmull-Rom = standard "smooth").
 *
 * For each segment between p1 and p2, the cubic bezier control points are:
 *   c1 = p1 + (p2 - p0) / 6
 *   c2 = p2 - (p3 - p1) / 6
 * with p0 = first.duplicate, p3 = last.duplicate at boundaries.
 */
private fun catmullRomPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) {
        path.moveTo(points[0].x, points[0].y)
        return path
    }

    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = if (i == 0) points[i] else points[i - 1]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i + 2 < points.size) points[i + 2] else p2

        val c1x = p1.x + (p2.x - p0.x) / 6f
        val c1y = p1.y + (p2.y - p0.y) / 6f
        val c2x = p2.x - (p3.x - p1.x) / 6f
        val c2y = p2.y - (p3.y - p1.y) / 6f
        path.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
    }
    return path
}
