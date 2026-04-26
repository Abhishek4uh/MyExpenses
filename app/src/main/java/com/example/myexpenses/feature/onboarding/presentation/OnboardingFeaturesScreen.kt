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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev2
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary

private data class FeatureTile(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
)

private val featureTiles = listOf(
    FeatureTile(Icons.Outlined.Mic, "Voice", Accents.Amber),
    FeatureTile(Icons.Outlined.Sms, "SMS sync", Accents.Violet),
    FeatureTile(Icons.Outlined.BarChart, "Analytics", Accents.Cyan),
    FeatureTile(Icons.Outlined.Description, "Export", Accents.Green),
)

@Composable
fun OnboardingFeaturesScreen(onNavigateToAuth: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .drawBehind {
                // Amber blob — top-left
                val c1 = Offset(size.width * 0.18f, size.height * 0.28f)
                val r1 = size.width * 0.60f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Accents.Amber.copy(0.28f), Color.Transparent),
                        center = c1, radius = r1
                    ),
                    radius = r1, center = c1
                )
                // Warm blob — bottom-right
                val c2 = Offset(size.width * 0.88f, size.height * 0.85f)
                val r2 = size.width * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFC18B4F).copy(0.28f), Color.Transparent),
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
            // Skip
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
                    modifier = Modifier.clickable(onClick = onNavigateToAuth)
                )
            }

            // Feature tiles grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FeatureTilesGrid()
            }

            // Text area
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Smart features\nbuilt in.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "Auto-detect bank SMS, voice input, widgets, and beautiful analytics.",
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
                PageDotsIndicator(currentPage = 1, totalPages = 2)

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
                        .clickable(onClick = onNavigateToAuth)
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Get Started",
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
internal fun FeatureTilesGrid() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            featureTiles.take(2).forEach { tile ->
                FeatureTileCard(tile = tile, modifier = Modifier.fillMaxWidth())
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            featureTiles.drop(2).forEach { tile ->
                FeatureTileCard(tile = tile, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun FeatureTileCard(tile: FeatureTile, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BgElev2)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tile.tint.copy(0.14f))
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.label,
                tint = tile.tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = tile.label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
