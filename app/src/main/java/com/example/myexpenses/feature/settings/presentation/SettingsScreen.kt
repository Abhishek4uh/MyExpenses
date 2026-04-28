package com.example.myexpenses.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableRows
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev4
import com.example.myexpenses.core.ui.theme.BorderSubtle
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import com.example.myexpenses.feature.main.presentation.BottomNavBarReservedHeight

@Composable
fun SettingsScreen(
    onNavigateToDetail: (key: String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()){

    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val firstLetter = prefs.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    // ── SMS permission state ─────────────────────────────────────────────────
    var showSmsRationale by remember { mutableStateOf(false) }
    var smsPermanentlyDenied by remember { mutableStateOf(false) }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleSmsReader(true)
            smsPermanentlyDenied = false
        }
        else {
            viewModel.toggleSmsReader(false)
            smsPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, Manifest.permission.READ_SMS
            )
        }
    }

    // ── Reminders permission state ───────────────────────────────────────────
    var showRemindersRationale by remember { mutableStateOf(false) }
    val remindersPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) {granted ->
        if (granted) viewModel.toggleReminders(true)
    }

    // ── Delete confirmation ──────────────────────────────────────────────────
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val bottomBarInset = BottomNavBarReservedHeight +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showSmsRationale){
        AlertDialog(
            onDismissRequest = { showSmsRationale = false },
            containerColor = BgElev1,
            title = { Text("SMS Access", color = TextPrimary, fontFamily = InterFamily, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "To auto-detect bank & UPI transactions, we need to read incoming SMS.\n\nMessages are processed on-device — never sent to any server.",
                    color = TextTertiary, fontFamily = InterFamily, fontSize = 14.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSmsRationale = false; smsPermissionLauncher.launch(Manifest.permission.READ_SMS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Accents.Amber, contentColor = BgBase),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Allow", fontFamily = InterFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showSmsRationale = false }) {
                    Text("Not now", color = TextMuted, fontFamily = InterFamily)
                }
            }
        )
    }

    if (showRemindersRationale){
        AlertDialog(
            onDismissRequest = { showRemindersRationale = false },
            containerColor = BgElev1,
            title = { Text("Daily Reminders", color = TextPrimary, fontFamily = InterFamily, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "Get a gentle nudge at 12 PM and 12 AM to log your expenses. Notifications stay on-device.",
                    color = TextTertiary, fontFamily = InterFamily, fontSize = 14.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRemindersRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            remindersPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleReminders(true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accents.Amber, contentColor = BgBase),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Allow", fontFamily = InterFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showRemindersRationale = false }) {
                    Text("Not now", color = TextMuted, fontFamily = InterFamily)
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = BgElev1,
            title = { Text("Delete all data?", color = Danger, fontFamily = InterFamily, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "This permanently removes all transactions and cannot be undone.",
                    color = TextTertiary, fontFamily = InterFamily, fontSize = 14.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; viewModel.deleteAllData() },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = BgBase),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Delete", fontFamily = InterFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted, fontFamily = InterFamily)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            // Sit below the status bar — MainScreen no longer wraps screens
            // in a Scaffold, so each screen is responsible for its own top
            // inset.
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())){
        // Heading
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Settings",
            fontFamily = SerifFamily,
            fontSize = 36.sp,
            fontStyle = FontStyle.Normal,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = Spacing.xxl)
        )

        // Profile card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .padding(horizontal = Spacing.xxl)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Accents.Amber.copy(0.1f), Color.Transparent)
                    )
                )
                .border(1.dp, Accents.Amber.copy(0.2f), RoundedCornerShape(18.dp))
                .clickable { onNavigateToDetail("profile") }
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Accents.Amber, Color(0xFF1A8A48))
                        )
                    )
            ) {
                Text(
                    text = firstLetter,
                    fontFamily = InterFamily,
                    fontSize = 18.sp,
                    color = BgBase,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = prefs.name.ifEmpty { "You" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Member since Apr 2026",
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = TextTertiary,
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(Spacing.xxl))

        // SMART FEATURES
        SettingsSection(
            title = "Smart Features",
            items = listOf(
                SettingsRow.Toggle(
                    icon = Icons.Outlined.Sms,
                    label = "Auto-read SMS",
                    sub = if (smsPermanentlyDenied) "Denied — tap to open Settings" else "Detect bank & UPI messages",
                    checked = prefs.isSmsReaderEnabled,
                    onToggle = { enabled ->
                        if (enabled) {
                            if (smsPermanentlyDenied) {
                                val intent = Intent(SystemSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } else {
                                showSmsRationale = true
                            }
                        } else {
                            viewModel.toggleSmsReader(false)
                        }
                    },
                ),
                SettingsRow.Toggle(
                    icon = Icons.Outlined.NotificationsNone,
                    label = "Daily reminders",
                    sub = "12 PM & 12 AM nudges",
                    checked = prefs.isRemindersEnabled,
                    onToggle = { enabled ->
                        if (enabled) showRemindersRationale = true
                        else viewModel.toggleReminders(false)
                    },
                ),
                SettingsRow.Click(
                    icon = Icons.Outlined.Mic,
                    label = "Voice input",
                    sub = "On-device speech recognition",
                    onClick = { onNavigateToDetail("voice") },
                ),
            )
        )

        Spacer(Modifier.height(Spacing.xxl))

        // DATA
        SettingsSection(
            title = "Data",
            items = listOf(
                SettingsRow.Click(
                    icon = Icons.Outlined.Wallet,
                    label = "Manage accounts",
                    sub = "3 wallets connected",
                    onClick = { onNavigateToDetail("accounts") },
                ),
                SettingsRow.Click(
                    icon = Icons.Outlined.Tag,
                    label = "Categories",
                    sub = "11 default + 0 custom",
                    onClick = { onNavigateToDetail("categories") },
                ),
                SettingsRow.Click(
                    icon = Icons.Rounded.PictureAsPdf,
                    label = "Export PDF report",
                    sub = "Last 30 days",
                    onClick = { onNavigateToDetail("export_pdf") },
                ),
                SettingsRow.Click(
                    icon = Icons.Rounded.TableRows,
                    label = "Export CSV",
                    sub = "All transactions",
                    onClick = { onNavigateToDetail("export_csv") },
                ),
            )
        )

        Spacer(Modifier.height(Spacing.xxl))

        // APP
        SettingsSection(
            title = "App",
            items = listOf(
                SettingsRow.Click(
                    icon = Icons.Outlined.Info,
                    label = "About MyEx-pense",
                    onClick = { onNavigateToDetail("about") },
                ),
                SettingsRow.Click(
                    icon = Icons.Outlined.Star,
                    label = "Rate the app",
                    onClick = { onNavigateToDetail("rate") },
                ),
                SettingsRow.Click(
                    icon = Icons.Rounded.DeleteForever,
                    label = "Delete all data",
                    labelColor = Danger,
                    onClick = { showDeleteConfirm = true },
                ),
            )
        )

        // Reserve room for the floating nav bar so the last "Delete all data"
        // row is fully accessible (not hidden behind the bar).
        Spacer(Modifier.height(Spacing.huge + bottomBarInset))
    }
}

private sealed interface SettingsRow {
    data class Toggle(
        val icon: ImageVector,
        val label: String,
        val sub: String = "",
        val checked: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : SettingsRow

    data class Click(
        val icon: ImageVector,
        val label: String,
        val sub: String = "",
        val labelColor: Color = TextPrimary,
        val onClick: () -> Unit,
    ) : SettingsRow
}

@Composable
private fun SettingsSection(title: String, items: List<SettingsRow>) {
    Column(modifier = Modifier.padding(horizontal = Spacing.xxl)) {
        Text(
            text = title.uppercase(),
            fontFamily = InterFamily,
            fontSize = 11.sp,
            color = TextMuted,
            letterSpacing = 0.12.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(BgElev1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            items.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFF161616),
                    )
                }
                when (row) {
                    is SettingsRow.Toggle -> ToggleRow(row)
                    is SettingsRow.Click  -> ClickRow(row)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(row: SettingsRow.Toggle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        RowIcon(row.icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(row.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            if (row.sub.isNotEmpty()) {
                Text(row.sub, fontFamily = InterFamily, fontSize = 12.sp, color = TextTertiary)
            }
        }
        Switch(
            checked = row.checked,
            onCheckedChange = row.onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgBase,
                checkedTrackColor = Accents.Amber,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = Color(0xFF222222),
                uncheckedBorderColor = Color(0xFF222222),
            )
        )
    }
}

@Composable
private fun ClickRow(row: SettingsRow.Click) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = row.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        RowIcon(row.icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(row.label, style = MaterialTheme.typography.bodyMedium, color = row.labelColor)
            if (row.sub.isNotEmpty()) {
                Text(row.sub, fontFamily = InterFamily, fontSize = 12.sp, color = TextTertiary)
            }
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BgElev4)){
        Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
}
