package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.BgElev5
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.feature.analytics.domain.InsightsRange
import java.time.LocalDate

/**
 * Three-tab segmented filter: Week / Month / Year.
 *
 * Tapping a tab always resets to the *current* period — back/forward
 * navigation is handled by [PeriodNavigator].
 */
@Composable
fun TimeFilter(
    range: InsightsRange,
    onRangeChange: (InsightsRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val today  = remember { LocalDate.now() }

    val tabs = remember(today) {
        listOf(
            InsightsRange.Week(today)                          to "Week",
            InsightsRange.Month(today.year, today.monthValue) to "Month",
            InsightsRange.Year(today.year)                    to "Year",
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF101010))
            .border(1.dp, Color(0xFF1C1C1C), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        tabs.forEach { (option, label) ->
            TabPill(
                label    = label,
                selected = range::class == option::class,
                onClick  = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRangeChange(option)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg   by animateColorAsState(
        targetValue   = if (selected) BgElev5 else Color.Transparent,
        animationSpec = tween(200),
        label         = "tab_bg",
    )
    val tint by animateColorAsState(
        targetValue   = if (selected) TextPrimary else TextMuted,
        animationSpec = tween(200),
        label         = "tab_text",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(36.dp)
            .then(if (selected) Modifier.shadow(2.dp, RoundedCornerShape(10.dp)) else Modifier)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            label,
            fontSize   = 13.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color      = tint,
            maxLines   = 1,
        )
    }
}
