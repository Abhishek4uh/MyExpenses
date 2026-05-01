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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextDisabled
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary

/**
 * Period navigator: `‹  18-Apr-2026 – 24-Apr-2026  ›`
 *
 * Arrows are rendered as circular icon buttons that dim when disabled.
 * Haptic feedback fires on every enabled tap.
 */
@Composable
fun PeriodNavigator(
    label: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev1)
            .border(1.dp, BorderDefault, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        NavArrow(
            icon      = Icons.Rounded.ChevronLeft,
            enabled   = canGoBack,
            contentDescription = "Previous period",
            onClick   = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onBack()
            },
        )

        Text(
            text       = label,
            fontSize   = 13.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            modifier   = Modifier.weight(1f),
        )

        NavArrow(
            icon      = Icons.Rounded.ChevronRight,
            enabled   = canGoForward,
            contentDescription = "Next period",
            onClick   = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onForward()
            },
        )
    }
}

@Composable
private fun NavArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue   = if (enabled) TextPrimary else TextDisabled,
        animationSpec = tween(150),
        label         = "nav_arrow_tint",
    )
    val bg by animateColorAsState(
        targetValue   = if (enabled) BgElev3 else BgElev1,
        animationSpec = tween(150),
        label         = "nav_arrow_bg",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = tint,
            modifier           = Modifier.size(20.dp),
        )
    }
}
