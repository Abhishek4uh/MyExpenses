package com.example.myexpenses.feature.analytics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.Success
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.analytics.domain.Insight
import com.example.myexpenses.feature.analytics.domain.InsightTone

/**
 * Stack of up to 2 smart insight cards. The first card is emphasis-styled
 * (tone-tinted gradient background + colored border). The rest use the plain
 * elevated-surface treatment so the eye is drawn to insight #1.
 */
@Composable
fun InsightCardStack(
    insights: List<Insight>,
    modifier: Modifier = Modifier){

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        insights.forEachIndexed { index, insight ->
            if (index == 0) EmphasisCard(insight) else PlainCard(insight)
        }
    }
}

@Composable
private fun EmphasisCard(insight: Insight) {
    val tone = toneColor(insight.tone)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(tone.copy(alpha = 0.15f), tone.copy(alpha = 0.04f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .border(1.dp, tone.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        InsightContent(insight, iconBg = tone)
    }
}

@Composable
private fun PlainCard(insight: Insight) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgElev1)
            .border(1.dp, BorderDefault, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        InsightContent(insight, iconBg = toneColor(insight.tone))
    }
}

@Composable
private fun InsightContent(insight: Insight, iconBg: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = null,
                tint = BgBase,
                modifier = Modifier.size(18.dp),
            )
        }
        Column {
            Text(
                insight.title,
                fontSize = 14.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                insight.body,
                fontSize = 12.5.sp,
                fontFamily = InterFamily,
                color = TextTertiary,
                lineHeight = 17.sp,
            )
        }
    }
}

private fun toneColor(tone: InsightTone): Color = when (tone) {
    InsightTone.WARN -> Danger
    InsightTone.GOOD -> Success
    InsightTone.INFO -> Accents.Amber
}
