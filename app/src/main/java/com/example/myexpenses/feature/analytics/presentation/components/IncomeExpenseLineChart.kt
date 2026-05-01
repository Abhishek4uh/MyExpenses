package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.myexpenses.feature.analytics.presentation.TrendSeries
import kotlin.math.roundToInt

/**
 * Two-line Catmull-Rom chart for Month / Year views.
 *
 * Income line (green) and expense line (red) animate in together on data change.
 * Dragging reveals a tooltip with the values at that point.
 */
@Composable
fun IncomeExpenseLineChart(
    incomeSeries: TrendSeries,
    expenseSeries: TrendSeries,
    periodSubtitle: String,
    modifier: Modifier = Modifier,
) {
    var triggered by remember(incomeSeries, expenseSeries) { mutableStateOf(false) }
    LaunchedEffect(incomeSeries, expenseSeries) { triggered = true }

    val progress by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label         = "line_progress",
    )
    val fillAlpha by animateFloatAsState(
        targetValue   = if (triggered) 0.20f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label         = "line_fill_alpha",
    )

    val n = incomeSeries.values.size.coerceAtLeast(1)
    var tooltipIndex by remember(incomeSeries) { mutableIntStateOf(-1) }
    var dragX by remember { mutableFloatStateOf(0f) }

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
                        periodSubtitle,
                        fontSize   = 16.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LineLegend(Success, "Income")
                    LineLegend(Danger, "Expense")
                }
            }

            Spacer(Modifier.height(14.dp))

            val allValues = incomeSeries.values + expenseSeries.values
            val maxV = allValues.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val widthDp  = maxWidth
                val heightDp = 150.dp
                val padding  = 10.dp

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightDp)
                        .pointerInput(incomeSeries, expenseSeries) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    dragX = offset.x
                                },
                                onDragEnd   = {
                                    tooltipIndex = -1
                                }){_,dragAmount ->
                                dragX = (dragX + dragAmount.x).coerceIn(0f, size.width.toFloat())
                                val pPx = 10.dp.toPx()
                                val wPx = size.width - pPx * 2
                                tooltipIndex = if (n <= 1) 0 else
                                    ((dragX - pPx) / (wPx / (n - 1).toFloat())).roundToInt().coerceIn(0, n - 1)
                            }
                        }){
                    val pPx= 10.dp.toPx()
                    val w = size.width - pPx * 2
                    val h = size.height - pPx * 2
                    val gridDash = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx()))

                    //Grid lines
                    listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                        drawLine(
                            color = Color(0xFF1A1A1A),
                            start = Offset(pPx, pPx + h * frac),
                            end = Offset(pPx + w, pPx + h * frac),
                            strokeWidth= 1.dp.toPx(),
                            pathEffect = gridDash,
                        )
                    }

                    fun pointsFor(series: TrendSeries): List<Offset> =
                        series.values.mapIndexed { i, v ->
                            val x = pPx + if (n == 1) w / 2f else w * i / (n - 1).toFloat()
                            val y = pPx + h - (v / maxV) * h * progress
                            Offset(x, y)
                        }

                    val incomePoints  = pointsFor(incomeSeries)
                    val expensePoints = pointsFor(expenseSeries)

                    fun drawSeries(points: List<Offset>, color: Color) {
                        if (points.isEmpty()) return
                        val linePath = catmullRomPath(points)

                        //Fill
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(points.last().x,  pPx + h)
                            lineTo(points.first().x, pPx + h)
                            close()
                        }
                        drawPath(
                            path  = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = fillAlpha), Color.Transparent),
                                startY = pPx,
                                endY   = pPx + h,
                            ),
                        )
                        //Line
                        drawPath(
                            path  = linePath,
                            color = color,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap   = StrokeCap.Round,
                                join  = StrokeJoin.Round,
                            ),
                        )
                    }

                    drawSeries(incomePoints, Success)
                    drawSeries(expensePoints, Danger)

                    // Tooltip vertical line
                    if (tooltipIndex in 0 until n && incomePoints.size > tooltipIndex) {
                        val tipX = incomePoints[tooltipIndex].x
                        drawLine(
                            color       = Color.White.copy(alpha = 0.12f),
                            start       = Offset(tipX, pPx),
                            end         = Offset(tipX, pPx + h),
                            strokeWidth = 1.5.dp.toPx(),
                        )
                        // Dots on both lines
                        listOf(incomePoints to Success, expensePoints to Danger).forEach { (pts, c) ->
                            if (tooltipIndex < pts.size) {
                                drawCircle(color = c.copy(alpha = 0.25f), radius = 6.dp.toPx(), center = pts[tooltipIndex])
                                drawCircle(color = c,                     radius = 3.dp.toPx(), center = pts[tooltipIndex])
                            }
                        }
                    }
                }

                // Tooltip box
                if (tooltipIndex in incomeSeries.values.indices && incomeSeries.values.isNotEmpty()) {
                    val density  = LocalDensity.current
                    val pPx      = with(density) { 10.dp.toPx() }
                    val totalW   = with(density) { widthDp.toPx() }
                    val w        = totalW - pPx * 2
                    val tipX     = pPx + if (n == 1) w / 2f else w * tooltipIndex / (n - 1).toFloat()
                    val boxW     = with(density) { 110.dp.toPx() }
                    val rawX     = tipX - boxW / 2f
                    val clampedX = rawX.coerceIn(0f, totalW - boxW)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(clampedX.roundToInt(), 4) }
                            .width(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgElev2)
                            .border(1.dp, BorderDefault, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            val label = incomeSeries.labels.getOrElse(tooltipIndex) { "" }
                            Text(label, fontSize = 9.sp, fontFamily = InterFamily, color = TextFaint)
                            Spacer(Modifier.height(3.dp))
                            TooltipValueRow("Income",  incomeSeries.values[tooltipIndex],  Success)
                            Spacer(Modifier.height(2.dp))
                            TooltipValueRow("Expense", expenseSeries.values.getOrElse(tooltipIndex) { 0L }, Danger)
                        }
                    }
                }
            }

            // X-axis labels
            if (incomeSeries.xTickIndices.isNotEmpty() && incomeSeries.labels.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    incomeSeries.xTickIndices.forEach { idx ->
                        Text(
                            incomeSeries.labels.getOrElse(idx) { "" },
                            fontSize   = 10.sp,
                            fontFamily = InterFamily,
                            color      = TextFaint,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TooltipValueRow(label: String, value: Long, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
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
private fun LineLegend(color: Color, label: String) {
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

private fun catmullRomPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) { path.moveTo(points[0].x, points[0].y); return path }

    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = if (i == 0) points[i] else points[i - 1]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i + 2 < points.size) points[i + 2] else p2
        path.cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y,
        )
    }
    return path
}

private fun Long.formatCompact(): String = when {
    this >= 100_000 -> "%.1fL".format(this / 100_000.0)
    this >= 1_000   -> "%.1fK".format(this / 1_000.0)
    else            -> "%,d".format(this)
}
