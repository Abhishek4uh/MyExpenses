package com.example.myexpenses.feature.onboarding.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * Combined onboarding flow with HorizontalPager — supports both swipe and tap navigation.
 * Page 0: Welcome (OverviewIllustration + headline)
 * Page 1: Features (FeatureTiles + headline)
 */
@Composable
fun OnboardingScreen(onNavigateToAuth: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    fun advance() {
        if (pagerState.currentPage < 1) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        } else {
            onNavigateToAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .drawBehind {
                // Amber blob — top-right (visible on both pages)
                val c1 = Offset(size.width * 0.85f, size.height * 0.18f)
                val r1 = size.width * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Accents.Amber.copy(0.25f), Color.Transparent),
                        center = c1, radius = r1
                    ),
                    radius = r1, center = c1
                )
                // Violet blob — bottom-left
                val c2 = Offset(size.width * 0.10f, size.height * 0.88f)
                val r2 = size.width * 0.60f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF7A5BC9).copy(0.22f), Color.Transparent),
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
            // Skip / top bar
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
                    modifier = Modifier.clickable { onNavigateToAuth() }
                )
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    else -> FeaturesPage()
                }
            }

            // Bottom bar: dots + FAB
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageDotsIndicator(
                    currentPage = pagerState.currentPage,
                    totalPages = 2
                )
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
                        .clickable { advance() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = if (pagerState.currentPage == 0) "Next" else "Get Started",
                        tint = BgBase,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

// ─── Page content composables ─────────────────────────────────────────────────

@Composable
private fun WelcomePage() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Illustration
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OverviewIllustration()
        }
        // Text
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Track every rupee,\neffortlessly.",
                style = MaterialTheme.typography.headlineLarge,
                color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
                text = "Manual, voice or SMS — three ways to log expenses without breaking flow.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextTertiary,
            )
        }
    }
}

@Composable
private fun FeaturesPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Feature tiles grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FeatureTilesGrid()
        }
        // Text
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Smart features\nbuilt in.",
                style = MaterialTheme.typography.headlineLarge,
                color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
                text = "Auto-detect bank SMS, voice input, and beautiful analytics.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextTertiary,
            )
        }
    }
}

// ─── Shared dot indicator ─────────────────────────────────────────────────────

@Composable
internal fun PageDotsIndicator(currentPage: Int, totalPages: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(totalPages) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 22.dp else 6.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "dot_width"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == currentPage) Accents.Amber else Accents.Amber.copy(0.3f))
            )
        }
    }
}
