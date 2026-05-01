package com.example.myexpenses.feature.streak.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myexpenses.core.common.DayStatus
import com.example.myexpenses.core.common.StreakData
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextFaint
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Fire palette — scoped here, not in global theme
private val FlameOrange    = Color(0xFFFF6B35)
private val FlameBgActive  = Color(0xFFFF6B35).copy(alpha = 0.20f)
private val FlameGlow      = Color(0xFFFF6B35).copy(alpha = 0.28f)
private val FlameBgMissed  = Color(0xFF1D1D1D)

private val WEEKDAYS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

@Composable
fun StreakCalendarSheet(
    streakData: StreakData,
    displayMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDismiss: () -> Unit,
) {
    val canGoBack    = displayMonth > YearMonth.from(streakData.onboardingDate)
    val canGoForward = displayMonth < YearMonth.now()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(BgElev1)
                .border(1.dp, BorderDefault, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Month navigation
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousMonth, enabled = canGoBack) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = if (canGoBack) TextPrimary else TextFaint,
                    )
                }
                Text(
                    text       = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize   = 14.sp,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                IconButton(onClick = onNextMonth, enabled = canGoForward) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Next month",
                        tint = if (canGoForward) TextPrimary else TextFaint,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Weekday header
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAYS.forEach { label ->
                    Text(
                        label,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center,
                        fontSize   = 11.sp,
                        fontFamily = InterFamily,
                        color      = TextTertiary,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Calendar grid
            CalendarGrid(month = displayMonth, streakData = streakData)
        }
    }
}

// ─── Grid ──────────────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(month: YearMonth, streakData: StreakData) {
    val today       = LocalDate.now()
    val firstDay    = month.atDay(1)
    val startOffset = firstDay.dayOfWeek.value - 1   // Mon = 0, Sun = 6
    val daysInMonth = month.lengthOfMonth()
    val totalCells  = startOffset + daysInMonth
    val rows        = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        // Empty grid slot (before month start / after month end)
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date   = month.atDay(dayNum)
                        val status = dayStatus(date, streakData.activeDays, streakData.onboardingDate)
                        DayCell(
                            dayNumber = dayNum,
                            isToday   = date == today,
                            status    = status,
                            modifier  = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private fun dayStatus(
    date: LocalDate,
    activeDays: Set<LocalDate>,
    onboardingDate: LocalDate,
): DayStatus? {
    if (date.isBefore(onboardingDate)) return null  // pre-onboarding → invisible
    val today = LocalDate.now()
    return when {
        date.isAfter(today) -> DayStatus.FUTURE
        date in activeDays  -> DayStatus.COMPLETED
        else                -> DayStatus.MISSED      // today with no transaction also MISSED
    }
}

// ─── Day cell ──────────────────────────────────────────────────────────────────

@Composable
private fun DayCell(
    dayNumber: Int,
    isToday: Boolean,
    status: DayStatus?,
    modifier: Modifier = Modifier,
) {
    if (status == null) {
        // Pre-onboarding: fully invisible, keeps grid intact
        Box(modifier.aspectRatio(1f))
        return
    }

    // Animations — always running so Compose doesn't warn about conditional compositions,
    // but only applied visually when status == COMPLETED
    val flicker = rememberInfiniteTransition(label = "flk_$dayNumber")
    val flickerAlpha by flicker.animateFloat(
        initialValue  = 0.75f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(600 + (dayNumber % 7) * 70, easing = LinearEasing),
            RepeatMode.Reverse,
        ),
        label = "fa_$dayNumber",
    )
    val flickerScale by flicker.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(850 + (dayNumber % 5) * 90, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "fs_$dayNumber",
    )

    val isCompleted = status == DayStatus.COMPLETED

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (isCompleted)
                    Modifier.graphicsLayer(scaleX = flickerScale, scaleY = flickerScale)
                else Modifier
            ),
    ) {
        // Glow behind the circle (rendered first → stays behind)
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer(alpha = flickerAlpha * 0.5f)
                    .clip(CircleShape)
                    .background(FlameGlow),
            )
        }

        // Circle background
        val bgColor = when (status) {
            DayStatus.COMPLETED -> FlameBgActive
            DayStatus.MISSED    -> FlameBgMissed
            DayStatus.FUTURE    -> Color.Transparent
            DayStatus.TODAY     -> Color.Transparent // won't reach here with new dayStatus logic
        }
        val todayBorder = if (isToday)
            Modifier.border(1.5.dp, Color.White.copy(alpha = 0.32f), CircleShape)
        else Modifier

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(bgColor)
                .then(todayBorder),
        ) {
            when (status) {
                DayStatus.COMPLETED -> Text(
                    "🔥",
                    fontSize = 15.sp,
                    modifier = Modifier.graphicsLayer(alpha = flickerAlpha),
                )
                DayStatus.MISSED -> Text(
                    "🔥",
                    fontSize = 13.sp,
                    modifier = Modifier.graphicsLayer(alpha = 0.20f),
                )
                DayStatus.FUTURE -> Text(
                    dayNumber.toString(),
                    fontSize   = 11.sp,
                    fontFamily = InterFamily,
                    color      = TextFaint.copy(alpha = 0.5f),
                )
                DayStatus.TODAY -> { /* won't reach */ }
            }
        }
    }
}
