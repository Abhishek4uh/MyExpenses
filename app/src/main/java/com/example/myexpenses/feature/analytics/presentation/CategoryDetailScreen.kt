package com.example.myexpenses.feature.analytics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BgElev4
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.presentation.components.TrendCard

@Composable
fun CategoryDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val group = state.group ?: run {
        // Group not found — show simple back button + message
        Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
            BackButton(onNavigateBack)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .verticalScroll(rememberScrollState())
    ) {
        BackButton(onNavigateBack)

        // Hero
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(group.tone.bg)
            ) {
                Icon(
                    group.icon,
                    contentDescription = group.displayName,
                    tint = group.tone.fg,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column {
                Text(
                    "CATEGORY",
                    fontSize = 12.sp,
                    fontFamily = InterFamily,
                    color = TextTertiary,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    group.displayName,
                    fontSize = 32.sp,
                    fontFamily = SerifFamily,
                    color = TextPrimary,
                    letterSpacing = (-0.4).sp,
                )
            }
        }

        // Big amount
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                "SPENT THIS MONTH",
                fontSize = 11.sp,
                fontFamily = InterFamily,
                color = TextMuted,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "₹",
                    fontSize = 36.sp,
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    color = TextPrimary,
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    "%,d".format(state.totalSpend),
                    fontSize = 44.sp,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-1.2).sp,
                )
            }
        }

        // Trend chart — tinted to group's accent
        TrendCard(
            trend = state.trend,
            totalLabel = "₹${"%,d".format(state.totalSpend)}",
            delta = 0f,                  // detail screen doesn't compute period delta
            accent = group.tone.fg,
            eyebrow = "Daily — ${state.periodLabel}",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(20.dp))

        // Sub-category list
        Text(
            "Sub-categories",
            fontSize = 14.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgElev1)
                .border(1.dp, BorderDefault, RoundedCornerShape(20.dp))
        ) {
            state.subCategories.forEachIndexed { i, sub ->
                if (i > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BgElev3)
                    )
                }
                SubCategoryRow(sub, group.tone.fg, group.tone.bg)
            }
        }

        Spacer(Modifier.height(40.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onClick) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BgElev3)
                    .border(1.dp, BorderDefault, CircleShape),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SubCategoryRow(
    sub: SubCategoryStat,
    fgColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
        ) {
            Text(sub.category.emoji, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                sub.category.displayName,
                fontSize = 13.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BgElev4)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sub.pct.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(fgColor, RoundedCornerShape(2.dp))
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "₹${"%,d".format(sub.amount)}",
                fontSize = 14.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                "${(sub.pct * 100).toInt()}%",
                fontSize = 11.sp,
                fontFamily = InterFamily,
                color = TextMuted,
            )
        }
    }
}
