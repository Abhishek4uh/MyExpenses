package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev5
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextDisabled
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.domain.InsightsRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Segmented filter: Week / Month / Year + Custom (date range picker).
 * Persists selection via [onRangeChange] (parent ViewModel writes to DataStore).
 */
@Composable
fun TimeFilter(
    range: InsightsRange,
    onRangeChange: (InsightsRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showPicker) {
        DateRangePickerDialog(
            initialRange = range as? InsightsRange.Custom,
            onDismiss = { showPicker = false },
            onApply = { start, end ->
                showPicker = false
                onRangeChange(InsightsRange.Custom(start, end))
            },
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Three fixed segments
        listOf(InsightsRange.Week, InsightsRange.Month, InsightsRange.Year).forEach { option ->
            FilterPill(
                label = option.shortLabel(),
                selected = range::class == option::class,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRangeChange(option)
                },
                modifier = Modifier.weight(1f),
            )
        }

        // Calendar icon (or compact custom-range pill when active)
        if (range is InsightsRange.Custom) {
            FilterPill(
                label = range.shortLabel(),
                selected = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showPicker = true
                },
                modifier = Modifier.weight(1.4f),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 44.dp, height = 36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showPicker = true
                    }
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "Custom range",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) BgElev5 else Color.Transparent,
        animationSpec = tween(200),
        label = "filter_pill_bg",
    )
    val tint by animateColorAsState(
        targetValue = if (selected) TextPrimary else TextMuted,
        animationSpec = tween(200),
        label = "filter_pill_text",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(36.dp)
            .then(
                if (selected) Modifier.shadow(2.dp, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color = tint,
            maxLines = 1,
        )
    }
}

/**
 * Custom-themed date range dialog. Built on raw [Dialog] (not Material3's
 * [androidx.compose.material3.DatePickerDialog]) so we can:
 *  - apply a horizontal margin so the dialog doesn't span full width
 *  - draw a subtle amber gradient border around the surface
 *  - drop the default "Pick a range" title (it never aligned cleanly)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    initialRange: InsightsRange.Custom?,
    onDismiss: () -> Unit,
    onApply: (LocalDate, LocalDate) -> Unit,
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialRange?.start
            ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = initialRange?.end
            ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    )
    val pickerColors = appDatePickerColors()

    Dialog(
        onDismissRequest = onDismiss,
        // false → we handle width ourselves via Modifier.widthIn / horizontal padding
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                // Tight horizontal margin (8dp) so the calendar's last day
                // column is never clipped on smaller phones. Material3's
                // DateRangePicker calculates 7 equal-width day cells — any
                // extra padding around it eats into the rightmost column.
                .padding(horizontal = 8.dp)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(BgElev1)
                .border(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Accents.Amber.copy(alpha = 0.45f),
                            Accents.Amber.copy(alpha = 0.10f),
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.04f),
                        )
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Scale the entire picker down ~12% so the 7 day columns +
                // their internal padding fit within the dialog's narrower
                // content area on small phones. Density-based scaling shrinks
                // text, cells, AND padding proportionally — much cleaner than
                // overriding individual typography slots.
                val baseDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides androidx.compose.ui.unit.Density(
                        density = baseDensity.density * 0.88f,
                        fontScale = baseDensity.fontScale * 0.88f,
                    )
                ) {
                    DateRangePicker(
                        state = pickerState,
                        colors = pickerColors,
                        showModeToggle = false,
                        // Drop the built-in title — the Material3 default sits
                        // misaligned against our edge-to-edge layout.
                        title = null,
                        headline = { CustomHeadline(pickerState) },
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }

                // Action row — Cancel + Apply. Right-aligned, app palette.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Cancel",
                            color = TextMuted,
                            fontFamily = InterFamily,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val s = pickerState.selectedStartDateMillis
                            val e = pickerState.selectedEndDateMillis
                            if (s != null && e != null) {
                                val sd = Instant.ofEpochMilli(s).atZone(ZoneId.systemDefault()).toLocalDate()
                                val ed = Instant.ofEpochMilli(e).atZone(ZoneId.systemDefault()).toLocalDate()
                                onApply(sd, ed)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accents.Amber,
                            contentColor = BgBase,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Apply", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Compact selected-range headline rendered above the calendar grid.
 * Shows the picked range as `7-Jan-26 - 8-May-26` (Inter SemiBold) so the
 * dates fit comfortably on one line without truncation, replacing
 * Material3's default oversized two-line headline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomHeadline(
    pickerState: androidx.compose.material3.DateRangePickerState,
) {
    val fmt = remember { DateTimeFormatter.ofPattern("d-MMM-yy") }

    fun Long.toDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    val s = pickerState.selectedStartDateMillis
    val e = pickerState.selectedEndDateMillis
    val text = when {
        s == null -> "Select start date"
        e == null -> "${s.toDate().format(fmt)} – select end"
        else      -> "${s.toDate().format(fmt)}  →  ${e.toDate().format(fmt)}"
    }

    Text(
        text = text,
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = TextPrimary,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
    )
}

/**
 * Dark-mode date picker palette that mirrors the rest of the app:
 *  - card surface uses [BgElev1]
 *  - selected day / today / range fill use [Accents.Amber]
 *  - in-range fill is amber @ 18% so the range chunk reads softly
 *  - disabled / muted dates use [TextDisabled] / [TextMuted]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appDatePickerColors(): DatePickerColors = DatePickerDefaults.colors(
    containerColor = BgElev1,
    titleContentColor = TextPrimary,
    headlineContentColor = TextPrimary,
    weekdayContentColor = TextMuted,
    subheadContentColor = TextTertiary,
    navigationContentColor = TextPrimary,
    yearContentColor = TextPrimary,
    disabledYearContentColor = TextDisabled,
    currentYearContentColor = Accents.Amber,
    selectedYearContentColor = BgBase,
    disabledSelectedYearContentColor = TextDisabled,
    selectedYearContainerColor = Accents.Amber,
    disabledSelectedYearContainerColor = BgElev5,
    dayContentColor = TextPrimary,
    disabledDayContentColor = TextDisabled,
    selectedDayContentColor = BgBase,
    disabledSelectedDayContentColor = TextDisabled,
    selectedDayContainerColor = Accents.Amber,
    disabledSelectedDayContainerColor = BgElev5,
    todayContentColor = Accents.Amber,
    todayDateBorderColor = Accents.Amber.copy(alpha = 0.55f),
    dayInSelectionRangeContainerColor = Accents.Amber.copy(alpha = 0.18f),
    dayInSelectionRangeContentColor = Accents.Amber,
)
