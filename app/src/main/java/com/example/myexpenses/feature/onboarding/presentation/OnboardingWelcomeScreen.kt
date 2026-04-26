package com.example.myexpenses.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary

@Composable
fun OnboardingWelcomeScreen(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .drawBehind {
                // Amber blob — top-right
                val c1 = Offset(size.width * 0.85f, size.height * 0.18f)
                val r1 = size.width * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Accents.Amber.copy(0.28f), Color.Transparent),
                        center = c1, radius = r1
                    ),
                    radius = r1, center = c1
                )
                // Violet blob — bottom-left
                val c2 = Offset(size.width * 0.10f, size.height * 0.88f)
                val r2 = size.width * 0.60f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF7A5BC9).copy(0.28f), Color.Transparent),
                        center = c2, radius = r2
                    ),
                    radius = r2, center = c2
                )
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xxl, vertical = Spacing.xl),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.clickable(onClick = onNext)
                )
            }

            // Illustration — ₹ circle + floating cards
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OverviewIllustration()
            }

            // Text area
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Track every rupee,\neffortlessly.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "Manual, voice or SMS — three ways to log expenses without breaking flow.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextTertiary,
                )
            }

            // Pager dots + FAB
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageDotsIndicator(currentPage = 0, totalPages = 2)

                // Arrow FAB
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            ambientColor = Accents.Amber.copy(0.55f),
                            spotColor = Accents.Amber.copy(0.7f)
                        )
                        .clip(CircleShape)
                        .background(Accents.Amber)
                        .clickable(onClick = onNext)
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Next",
                        tint = BgBase,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
internal fun OverviewIllustration() {
    Box(modifier = Modifier.size(280.dp)) {
        // Big ₹ circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Accents.Amber.copy(0.08f))
        ) {
            Text(
                text = "₹",
                fontSize = 130.sp,
                fontFamily = SerifFamily,
                fontStyle = FontStyle.Italic,
                color = Accents.Amber.copy(0.7f),
                lineHeight = 130.sp
            )
        }

        // Floating card — Swiggy (top-right)
        FloatingCard(
            dot = Danger,
            text = "Swiggy ₹320",
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Floating card — Salary (bottom-left)
        FloatingCard(
            dot = Accents.Amber,
            text = "Salary +₹85,000",
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // Floating card — Uber (bottom-right, mid)
        FloatingCard(
            dot = Accents.Violet,
            text = "Uber ₹1,200",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 52.dp)
        )
    }
}

@Composable
private fun FloatingCard(dot: Color, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgElev3)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dot)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = InterFamily,
            color = TextPrimary
        )
    }
}

