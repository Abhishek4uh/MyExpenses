package com.example.myexpenses.feature.about.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInNew
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BorderSubtle
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextSecondary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.main.presentation.BottomNavBarReservedHeight

@Composable
fun AboutScreen(onNavigateToDetail: () -> Unit) {
    val bottomBarInset = BottomNavBarReservedHeight +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            // Sit below the status bar — MainScreen's Box overlay no longer
            // applies a top inset, so each screen handles its own.
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xxxl, bottom = Spacing.xxl)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Accents.Amber.copy(0.4f),
                        spotColor = Accents.Amber.copy(0.5f),
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(listOf(Accents.Amber, Color(0xFF1A8A48)))
                    )
            ) {
                Text(
                    text = "₹",
                    fontSize = 50.sp,
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    color = BgBase,
                    lineHeight = 50.sp,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "MyEx-pense",
                fontFamily = SerifFamily,
                fontSize = 32.sp,
                color = TextPrimary,
                letterSpacing = (-0.02 * 32).sp,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "v1.0.0 · Build 2026.04",
                fontFamily = InterFamily,
                fontSize = 13.sp,
                color = TextTertiary,
            )
        }

        // WHAT IT IS
        AboutCard(modifier = Modifier.padding(horizontal = Spacing.xxl)) {
            SectionLabel("What it is")
            Spacer(Modifier.height(14.dp))
            Text(
                text = "A modern, native expense manager built entirely on Kotlin, Jetpack Compose, and Material 3. No third-party ad networks. Your data stays on your device.",
                fontFamily = InterFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = (14 * 1.6).sp,
            )
        }

        Spacer(Modifier.height(14.dp))

        // TECH STACK
        AboutCard(modifier = Modifier.padding(horizontal = Spacing.xxl)) {
            SectionLabel("Tech Stack")
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                listOf(
                    "Kotlin · Jetpack Compose",
                    "Material 3 · MVVM",
                    "Room · DataStore",
                    "Coroutines · Flow",
                    "Native SpeechRecognizer",
                    "Native Telephony API",
                ).forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Accents.Amber)
                        )
                        Text(item, fontFamily = InterFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Links
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = Spacing.xxl)
        ) {
            LinkRow(icon = Icons.Outlined.OpenInNew, label = "View source on GitHub", onClick = {})
            LinkRow(icon = Icons.Outlined.Language, label = "Privacy & terms", onClick = {})
            LinkRow(icon = Icons.Outlined.Description, label = "Open-source licenses", onClick = {})
        }

        Spacer(Modifier.height(Spacing.xxxl))

        Text(
            text = "Made with care in India ❤️",
            fontFamily = InterFamily,
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = Spacing.xxl)
        )
        // Reserve room at the bottom for the floating nav bar so the credit
        // line above is visible (not trapped behind the bar).
        Spacer(Modifier.height(bottomBarInset))
    }
}

@Composable
private fun AboutCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgElev1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        content()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = InterFamily,
        fontSize = 11.sp,
        color = Accents.Amber,
        letterSpacing = 0.12.sp,
    )
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.weight(1f), fontFamily = InterFamily, fontSize = 14.sp, color = TextPrimary)
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}
