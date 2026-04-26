package com.example.myexpenses.feature.about.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myexpenses.core.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDetailScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About Expense Manager",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppColors.PrimaryContainer)
            ) {
                Text("₹", fontSize = 40.sp, color = AppColors.Primary, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(16.dp))
            Text("Expense Manager", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0 (Build 1)", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)

            Spacer(Modifier.height(28.dp))
            Divider(color = AppColors.Divider)
            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {

                Surface(shape = RoundedCornerShape(20.dp), color = AppColors.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("About", style = MaterialTheme.typography.titleSmall, color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Expense Manager is a free, privacy-first app to track your daily income and spending. Built with Kotlin and Jetpack Compose, it runs entirely offline with all data stored on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = AppColors.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("What's New in 1.0.0", style = MaterialTheme.typography.titleSmall, color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
                        listOf(
                            "🚀 Initial release",
                            "📊 Dashboard with balance overview",
                            "🎙️ Voice-to-expense input",
                            "📱 Bank SMS auto-detection",
                            "⚙️ Settings with reminders"
                        ).forEach { item ->
                            Text(item, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = AppColors.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("License", style = MaterialTheme.typography.titleSmall, color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
                        Text(
                            "This app is for personal use. All financial data remains on your device and is protected by your device's security.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("⭐  Rate this App", color = AppColors.Primary)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Made with ❤️ in India",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextDisabled,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
