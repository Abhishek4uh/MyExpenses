package com.example.myexpenses.feature.splash.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()){

    val destination by viewModel.destination.collectAsStateWithLifecycle()

    var reveal by remember { mutableStateOf(false) }

    val tileScale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tile_scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = tween(500, delayMillis = 250),
        label = "content_alpha"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = tween(900),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        reveal = true
    }

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Onboarding -> { delay(1800); onNavigateToOnboarding() }
            SplashDestination.Main       -> { delay(1800); onNavigateToMain() }
            SplashDestination.Loading    -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)){
        //Ambient radial glow
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.Center)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Accents.Amber.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        // Center content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)){
            // Gradient logo tile — ₹ in Instrument Serif Italic on colored bg
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .scale(tileScale)
                    .shadow(
                        elevation = 36.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Accents.Amber.copy(0.55f),
                        spotColor = Accents.Amber.copy(0.7f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Accents.Amber, Color(0xFF1A8A48))
                        )
                    )){
                Text(
                    text = "₹",
                    fontSize = 48.sp,
                    color = BgBase,
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                )
            }

            // Wordmark + tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alpha(contentAlpha)){
                Text(
                    text = "MyEx-pense",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "Money, mindfully tracked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Pulsing dots pinned to bottom
        PulsingDots(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(contentAlpha)
                .padding(bottom = 60.dp)
        )
    }
}

@Composable
private fun PulsingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier){
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_alpha_$index"
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}
