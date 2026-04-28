package com.example.myexpenses.feature.settings.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.notification.NotificationHelper
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.AppColors
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.TextFaint
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(
    settingsKey: String,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()){

    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    // Permission launcher for enabling daily reminders from the detail screen.
    // Without this, the toggle calls toggleReminders(true) directly which would
    // schedule alarms but notifications would silently drop on Android 13+ if the
    // POST_NOTIFICATIONS permission isn't granted.
    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.toggleReminders(true) }

    val title = when (settingsKey) {
        "profile" -> "Edit Profile"
        "notifications" -> "Reminders"
        "categories" -> "Categories"
        "export_pdf" -> "Export PDF"
        "export_csv" -> "Export CSV"
        "about" -> "About App"
        else -> "Settings"
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
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
        }){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)){
            when (settingsKey) {
                "profile" -> ProfileEditContent(
                    currentName = prefs.name,
                    onSave = { viewModel.updateName(it); onNavigateBack() }
                )
                "notifications" -> NotificationsContent(
                    enabled = prefs.isRemindersEnabled,
                    onToggle = { enabled ->
                        if (!enabled) {
                            viewModel.toggleReminders(false)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) viewModel.toggleReminders(true)
                            else notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleReminders(true)
                        }
                    },
                    onTestNotification = {
                        NotificationHelper.showReminderNotification(context, "Daily Reminder")
                    },
                )
                "categories" -> CategoriesContent(
                    stats = viewModel.categoryStats.collectAsStateWithLifecycle().value,
                )
                "export_pdf" -> ExportComingSoon(
                    icon = { Icon(Icons.Rounded.PictureAsPdf, null, tint = AppColors.Expense, modifier = Modifier.size(48.dp)) },
                    title = "PDF Export",
                    description = "Export your expense reports as beautifully formatted PDFs. Available in the next update."
                )
                "export_csv" -> ExportComingSoon(
                    icon = { Icon(Icons.Rounded.TableChart, null, tint = AppColors.Income, modifier = Modifier.size(48.dp)) },
                    title = "CSV Export",
                    description = "Export your transaction data as a CSV file for use in Excel or Google Sheets. Available in the next update."
                )
                "about" -> AboutContent()
                else -> ComingSoonContent()
            }
        }
    }
}

@Composable
private fun ProfileEditContent(currentName: String, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    val focusManager = LocalFocusManager.current
    val firstLetter = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        // Avatar preview
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accents.Amber, AppColors.Income)))
        ) {
            Text(firstLetter, fontFamily = InterFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BgBase)
        }

        // Name field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgElev3)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text("YOUR NAME", fontSize = 10.sp, fontFamily = InterFamily, color = TextMuted, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = name,
                onValueChange = { if (it.length <= 32) name = it },
                textStyle = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 26.sp,
                    color = TextPrimary,
                    fontStyle = FontStyle.Normal
                ),
                cursorBrush = SolidColor(Accents.Amber),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (name.isEmpty()) {
                            Text("Enter your name", fontFamily = SerifFamily, fontSize = 26.sp, color = TextFaint, fontStyle = FontStyle.Italic)
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = if (name.isNotEmpty()) Accents.Amber else BorderDefault, thickness = 1.dp)
        }

        Button(
            onClick = { onSave(name) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accents.Amber,
                contentColor = BgBase,
                disabledContainerColor = BgElev3,
                disabledContentColor = TextMuted
            )
        ) {
            Text("Save", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun NotificationsContent(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Stay on top of your finances with daily reminders to log expenses. " +
                "We'll nudge you at 12:00 AM and 12:00 PM every day.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )

        Spacer(Modifier.height(8.dp))

        // Single switch that controls both daily slots — they always toggle
        // together, so showing two separate switches just confused users.
        ReminderToggleCard(
            emoji = "🔔",
            title = "Daily Reminders",
            subtitle = "12:00 AM & 12:00 PM check-ins",
            checked = enabled,
            onCheckedChange = onToggle
        )

        if (enabled) {
            OutlinedButton(
                onClick = onTestNotification,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Accents.Amber.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Accents.Amber)
            ) {
                Text("Send test notification", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ReminderToggleCard(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit){
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.SurfaceCard){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(emoji, fontSize = 28.sp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = AppColors.TextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.Background,
                    checkedTrackColor = AppColors.Primary,
                    uncheckedThumbColor = AppColors.TextSecondary,
                    uncheckedTrackColor = AppColors.Border
                )
            )
        }
    }
}

@Composable
private fun CategoriesContent(stats: List<CategoryStatRow>){
    val expenses = stats.filter {
        it.type == TransactionType.EXPENSE
    }
    val income = stats.filter {
        it.type == TransactionType.INCOME
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (expenses.isNotEmpty()) {
            CategorySection(title = "Expense Categories", items = expenses)
        }
        if (income.isNotEmpty()) {
            CategorySection(title = "Income Categories", items = income)
        }
    }
}

@Composable
private fun CategorySection(title: String, items: List<CategoryStatRow>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        items.forEach { stat ->
            CategoryStatCard(stat)
        }
    }
}

@Composable
private fun CategoryStatCard(stat: CategoryStatRow) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceCard){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(stat.category.colorHex.toColorInt()).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stat.category.emoji, fontSize = 20.sp)
                }

                Column {
                    Text(
                        stat.category.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${stat.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Text(
                "₹${String.format(LocalLocale.current.platformLocale,"%.2f", stat.totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (stat.type == TransactionType.INCOME) AppColors.Income else AppColors.Expense,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExportComingSoon(
    icon: @Composable () -> Unit,
    title: String,
    description: String){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        icon()
        Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.PrimaryContainer
        ) {
            Text(
                "Coming Soon",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.Primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoCard(title = "App Name", value = "Expense Manager")
        InfoCard(title = "Version", value = "1.0.0 (1)")
        InfoCard(title = "Build", value = "Production")
        Spacer(Modifier.height(8.dp))
        Text(
            "All your financial data is stored locally on your device and never sent to any server.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceCard, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ComingSoonContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Text("🚧", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("Coming Soon", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
        Text("This feature is under development.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
    }
}
