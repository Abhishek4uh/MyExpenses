package com.example.myexpenses.feature.home.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.feature.main.presentation.BottomNavBarReservedHeight
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev2
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BgElev5
import com.example.myexpenses.core.ui.theme.CategoryTones
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.Success
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextSecondary
import com.example.myexpenses.core.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.provider.Settings as SystemSettings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (transactionId: String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAllTransactions: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()){

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    //Total reserved space at the bottom: floating nav bar + system nav inset.
    // Used everywhere this screen pads its scrollable content / FAB.
    val systemNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarInset = BottomNavBarReservedHeight + systemNavInset
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.period.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isSmsEnabled by viewModel.isSmsEnabled.collectAsStateWithLifecycle()
    var fabExpanded by remember { mutableStateOf(false) }

    // ── SMS permission flow (in-place) ───────────────────────────────────────
    var showSmsRationale by remember { mutableStateOf(false) }
    var smsPermanentlyDenied by remember { mutableStateOf(false) }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()){ granted ->
        if (granted) {
            viewModel.toggleSmsReader(true)
            smsPermanentlyDenied = false
        }
        else {
            smsPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, Manifest.permission.READ_SMS
            )
        }
    }

    if (showSmsRationale){
        AlertDialog(
            onDismissRequest = { showSmsRationale = false },
            containerColor = BgElev1,
            title = { Text("Enable SMS sync", color = TextPrimary, fontFamily = InterFamily, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "We'll auto-detect bank & UPI transactions from your SMS inbox. Messages stay on-device — never sent to any server.",
                    color = TextTertiary, fontFamily = InterFamily, fontSize = 14.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSmsRationale = false
                        if (smsPermanentlyDenied) {
                            val intent = Intent(SystemSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        else {
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accents.Amber, contentColor = BgBase),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (smsPermanentlyDenied) "Open Settings" else "Allow", fontFamily = InterFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsRationale = false }) {
                    Text("Not now", color = TextMuted, fontFamily = InterFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = BgBase,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            // Pad the FAB upward by the bar inset so it sits above the
            // floating nav bar instead of being trapped behind it.
            Box(modifier = Modifier.padding(bottom = bottomBarInset)) {
                ExpandableFab(
                    expanded = fabExpanded,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = !fabExpanded
                    },
                    onVoice  = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false; onNavigateToDetail("voice")
                    },
                    onManual = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false; onNavigateToDetail("new")
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBase),
            // Bottom contentPadding stacks: FAB clearance (80) + the reserved
            // space for the floating nav bar (bar height + system nav). Last
            // list item is always accessible above both the FAB and the bar.
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + 80.dp + bottomBarInset
            )){
            // Top app bar — plain, avatar/greeting + sync action (no notif bell)
            item {
                HomeTopBar(
                    userName = userName,
                    isSmsEnabled = isSmsEnabled,
                    isSyncing = isSyncing,
                    onSyncClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.syncSmsTransactions()
                    }
                )
            }

            //Balance card — mesh-gradient block separate from the top bar
            item {
                BalanceGradientCard(
                    uiState = uiState,
                    selectedPeriod = selectedPeriod,
                    modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = Spacing.xs)
                )
            }

            //Period tabs — sticky so they remain visible while transactions scroll
            item {
                Box(Modifier.fillMaxWidth().background(Color.Transparent)) {
                    PeriodTabsRow(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = viewModel::selectPeriod,
                        modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = 10.dp)
                    )
                }
            }

            // SMS CTA banner — shown when SMS sync is disabled
            if (!isSmsEnabled) {
                item {
                    SmsCTABanner(
                        onEnable = { showSmsRationale = true },
                        modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = 4.dp)
                    )
                }
            }

            //Content
            when (val state = uiState) {
                HomeUiState.Loading -> item {
                    ShimmerContent()
                }
                is HomeUiState.Success -> {
                    item {
                        RecentActivityHeader(
                            hasTransactions = state.recentTransactions.isNotEmpty(),
                            onSeeAll = onNavigateToAllTransactions,
                            modifier = Modifier.padding(
                                horizontal = Spacing.xxl,
                                vertical = Spacing.xs
                            )
                        )
                    }

                    if (state.recentTransactions.isEmpty()) {
                        item { EmptyTransactions() }
                    }
                    else {
                        items(
                            items = state.recentTransactions,
                            key = {
                                it.id
                            }){ transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onNavigateToDetail(transaction.id) },
                                onDelete = { viewModel.deleteTransaction(transaction.id) },
                                modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = 3.dp)
                            )
                        }
                    }
                }
                is HomeUiState.Error -> item {
                    ErrorContent(
                        message = state.message,
                        onRetry = viewModel::syncSmsTransactions
                    )
                }
            }
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    userName: String,
    isSmsEnabled: Boolean,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
) {
    val displayName = userName.ifEmpty { "there" }
    val avatarLetter = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val greeting = when (java.time.LocalTime.now().hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else -> "Good evening,"
    }
    val infiniteTransition = rememberInfiniteTransition(label = "topbar_sync")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "topbar_sync_rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .statusBarsPadding()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically){
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Accents.Amber, Success)
                        )
                    )){
                Text(
                    text = avatarLetter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFamily,
                    color = BgBase
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = greeting,
                    fontSize = 12.sp,
                    fontFamily = InterFamily,
                    color = TextMuted
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        //Right-side sync section — replaces the old notification bell. Only
        //visible when SMS sync is enabled. Tap to trigger a manual rescan;
        //icon spins while a sync is in progress.
        if (isSmsEnabled){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgElev2)
                    .clickable(enabled = !isSyncing, onClick = onSyncClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)){
                Icon(
                    Icons.Outlined.Sync,
                    contentDescription = null,
                    tint = Accents.Amber,
                    modifier = Modifier
                        .size(14.dp)
                        .then(if (isSyncing) Modifier.rotate(syncRotation) else Modifier)
                )
                AnimatedVisibility(visible = isSyncing){
                    Text(
                        text = "Syncing",
                        fontSize = 11.sp,
                        fontFamily = InterFamily,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// ─── Balance Gradient Card ────────────────────────────────────────────────────

@Composable
private fun BalanceGradientCard(
    uiState: HomeUiState,
    selectedPeriod: DashboardPeriod,
    modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val blob1Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(8_000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "blob1"
    )
    val blob2Progress by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(10_000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "blob2"
    )
    val blob3Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(12_000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "blob3"
    )

    val stats = (uiState as? HomeUiState.Success)?.stats
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .drawBehind {
                // Base fill — slightly elevated so the card visually pops
                drawRect(color = BgElev1)
                // Blob 1 — Amber, top-left
                val b1x = size.width * (-0.20f + blob1Progress * 0.20f)
                val b1y = size.height * (-0.40f + blob1Progress * 0.10f)
                val r1 = size.width * 0.75f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Accents.Amber.copy(0.40f), Color.Transparent),
                        center = Offset(b1x, b1y), radius = r1
                    ),
                    radius = r1, center = Offset(b1x, b1y)
                )
                // Blob 2 — Violet, bottom-right
                val b2x = size.width * (1.30f - blob2Progress * 0.15f)
                val b2y = size.height * (1.30f - blob2Progress * 0.10f)
                val r2 = size.width * 0.80f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7A5BC9).copy(0.35f), Color.Transparent),
                        center = Offset(b2x, b2y), radius = r2
                    ),
                    radius = r2, center = Offset(b2x, b2y)
                )
                // Blob 3 — Warm, top-right
                val b3x = size.width * (1.10f - blob3Progress * 0.10f)
                val b3y = size.height * (0.20f + blob3Progress * 0.15f)
                val r3 = size.width * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC18B4F).copy(0.30f), Color.Transparent),
                        center = Offset(b3x, b3y), radius = r3
                    ),
                    radius = r3, center = Offset(b3x, b3y)
                )
            }){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg)){
            Text(
                text = "TOTAL BALANCE",
                fontSize = 11.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(6.dp))

            // Big balance number — count-up + bouncy "throw in" replays each
            // time the underlying amount changes (e.g. period switch).
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "₹",
                    fontSize = 26.sp,
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    color = TextPrimary,
                    lineHeight = 56.sp
                )
                when (val s = stats) {
                    null -> Text(
                        text = "—",
                        style = MaterialTheme.typography.displayLarge,
                        color = TextPrimary
                    )
                    else -> AnimatedBalance(amount = s.netBalance)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Income / expense trend row — unchanged from before
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                TrendPill(
                    icon = { Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(11.dp)) },
                    amount = stats?.totalIncome ?: 0.0,
                    color = Success
                )
                TrendPill(
                    icon = { Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(11.dp)) },
                    amount = stats?.totalExpense ?: 0.0,
                    color = Danger
                )
            }
        }
    }
}

private fun periodLabel(period: DashboardPeriod): String = when (period) {
    DashboardPeriod.TODAY -> "Today"
    DashboardPeriod.WEEK  -> "This week"
    DashboardPeriod.MONTH -> "This month"
    DashboardPeriod.YEAR  -> "This year"
}

/**
 * Dramatic "throw, bounce, land" animation. Every time the amount changes:
 *  - The number is yanked off-screen above with a heavy tilt, tiny scale, and
 *    nearly transparent — it's been thrown up like a ball.
 *  - Independent springs drag it back down: a heavy-bounce drop for Y, a
 *    bouncy scale that overshoots past 1.0 and oscillates back, a wobbling
 *    rotation that crosses zero and counter-tilts before settling.
 *  - The digits race upward via a fast tween so you can see them cycling
 *    while the number is still Mid-air.
 *  - Once it's landed, a quick "punch" pulse (1.0 → 1.07 → 1.0) hits for
 *    that satisfying impact feel.
 *
 * The asymmetric spring tunings (different dampingRatio + stiffness per
 * channel) are what make it feel alive rather than mechanical.
 */
@Composable
private fun AnimatedBalance(amount: Double) {
    val displayAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        // Faster than the throw, so digits race past while the number bounces in.
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "balance_count"
    )

    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val translationY = remember { androidx.compose.animation.core.Animatable(0f) }
    val rotationZ = remember { androidx.compose.animation.core.Animatable(0f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(amount) {
        // ── 1. Throw pose: way up, tiny, tilted, ghosted ──
        scale.snapTo(0.35f)
        translationY.snapTo(-160f)
        rotationZ.snapTo(-14f)
        alpha.snapTo(0f)

        // Asymmetric springs — each channel oscillates with its own rhythm
        // so the motion feels organic rather than uniform.
        val dropSpec = androidx.compose.animation.core.spring<Float>(
            dampingRatio = 0.42f,           // very bouncy
            stiffness = 180f                // slow enough to be visible
        )
        val scaleSpec = androidx.compose.animation.core.spring<Float>(
            dampingRatio = 0.5f,
            stiffness = 240f
        )
        val rotationSpec = androidx.compose.animation.core.spring<Float>(
            dampingRatio = 0.45f,
            stiffness = 280f                // wobbles fastest
        )

        kotlinx.coroutines.coroutineScope {
            launch { scale.animateTo(1f, scaleSpec) }
            launch { translationY.animateTo(0f, dropSpec) }
            launch { rotationZ.animateTo(0f, rotationSpec) }
            // Fade in faster than the spring lands so the number is fully
            // visible while it's still bouncing.
            launch { alpha.animateTo(1f, tween(280, easing = FastOutSlowInEasing)) }
        }

        // ── 2. Punch on land: a final tight scale snap for impact ──
        kotlinx.coroutines.coroutineScope {
            launch {
                scale.animateTo(1.07f, tween(90, easing = FastOutSlowInEasing))
                scale.animateTo(
                    1f,
                    androidx.compose.animation.core.spring(
                        dampingRatio = 0.55f,
                        stiffness = 600f
                    )
                )
            }
        }
    }

    Text(
        text = String.format(LocalLocale.current.platformLocale, "%,.0f", displayAmount),
        style = MaterialTheme.typography.displayLarge,
        color = TextPrimary,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.translationY = translationY.value
            this.rotationZ = rotationZ.value
            this.alpha = alpha.value
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
        }
    )
}

@Composable
private fun SyncChip(
    syncing: Boolean,
    syncRotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Accents.Amber.copy(0.12f))
            .clickable(enabled = !syncing, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Sync,
            contentDescription = if (syncing) "Syncing" else "Sync now",
            tint = Accents.Amber,
            modifier = Modifier
                .size(12.dp)
                .then(if (syncing) Modifier.rotate(syncRotation) else Modifier)
        )
        Text(
            text = if (syncing) "Syncing SMS…" else "Sync SMS now",
            fontSize = 12.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Medium,
            color = Accents.Amber
        )
    }
}

@Composable
private fun TrendPill(
    icon: @Composable () -> Unit,
    amount: Double,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color.copy(0.15f))
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides color
            ) { icon() }
        }
        Text(
            text = "₹${String.format("%,.0f", amount)}",
            fontSize = 13.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

// ─── Period Tabs (segmented control) ─────────────────────────────────────────

@Composable
private fun PeriodTabsRow(
    selectedPeriod: DashboardPeriod,
    onPeriodSelected: (DashboardPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF101010))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DashboardPeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            val bgColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) BgElev5 else Color.Transparent,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tab_bg"
            )
            val textColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) TextPrimary else TextMuted,
                animationSpec = tween(220),
                label = "tab_text"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = period.label,
                    fontSize = 13.sp,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

// ─── Recent Activity Header ───────────────────────────────────────────────────

@Composable
private fun RecentActivityHeader(
    hasTransactions: Boolean,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent activity",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        if (hasTransactions) {
            // Icon-only chevron — tappable circle that opens the full list screen.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Accents.Amber.copy(alpha = 0.14f))
                    .clickable(onClick = onSeeAll)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "See all transactions",
                    tint = Accents.Amber,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ─── Transaction Item with Swipe-to-Delete ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )

    val tone = categoryToneFor(transaction.category)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF3D1F1A)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = Danger)
                    Text("Delete", color = Danger, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgElev1)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category icon container with tone colors
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tone.bg)
                ) {
                    Text(transaction.category.emoji, fontSize = 20.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = transaction.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = transaction.dateTime.toLocalDate().formatRelative(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary
                    )
                }
            }

            // Amount
            val amountColor = if (transaction.type == TransactionType.INCOME) Success else Danger
            val prefix = if (transaction.type == TransactionType.INCOME) "+ ₹" else "− ₹"
            Text(
                text = "$prefix${String.format(LocalLocale.current.platformLocale, "%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

private fun categoryToneFor(category: ExpenseCategory): com.example.myexpenses.core.ui.theme.CategoryTone =
    when (category) {
        ExpenseCategory.RENT          -> CategoryTones.Rent
        ExpenseCategory.COMMUTE       -> CategoryTones.Commute
        ExpenseCategory.DINEIN        -> CategoryTones.Food
        ExpenseCategory.ONLINE_FOOD   -> CategoryTones.OnlineFood
        ExpenseCategory.GROCERY       -> CategoryTones.Food
        ExpenseCategory.SHOPPING      -> CategoryTones.Personal
        ExpenseCategory.UTILITIES     -> CategoryTones.Utilities
        ExpenseCategory.INSURANCE     -> CategoryTones.Insurance
        ExpenseCategory.HEALTHCARE    -> CategoryTones.Healthcare
        ExpenseCategory.INVESTMENT    -> CategoryTones.Saving
        ExpenseCategory.PERSONALCARE  -> CategoryTones.Personal
        ExpenseCategory.ENTERTAINMENT -> CategoryTones.Entertainment
        else                          -> CategoryTones.Misc
    }

private fun java.time.LocalDate.formatRelative(): String {
    val today = LocalDate.now()
    return when (this) {
        today              -> "Today"
        today.minusDays(1) -> "Yesterday"
        else-> format(DateTimeFormatter.ofPattern("d MMM"))
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyTransactions() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp)
    ) {
        Text("💸", fontSize = 48.sp)
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Text(
            "Tap + to add your first expense",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
    }
}

// ─── Expandable FAB ───────────────────────────────────────────────────────────

@Composable
private fun ExpandableFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onVoice: () -> Unit,
    onManual: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                FabOption(
                    label = "Voice",
                    icon = { Icon(Icons.Outlined.Mic, "Voice input") },
                    containerColor = Accents.Amber,
                    contentColor = BgBase,
                    onClick = onVoice
                )
                FabOption(
                    label = "Manual",
                    icon = { Icon(Icons.Outlined.Edit, "Manual entry") },
                    containerColor = BgElev3,
                    contentColor = TextPrimary,
                    onClick = onManual
                )
            }
        }

        val fabRotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            label = "fab_rotate"
        )
        FloatingActionButton(
            onClick = onToggle,
            containerColor = Accents.Amber,
            contentColor = BgBase,
            modifier = Modifier.shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Accents.Amber.copy(0.5f),
                spotColor = Accents.Amber.copy(0.7f)
            )
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = if (expanded) "Close" else "Add",
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun FabOption(
    label: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(BgElev3)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            icon()
        }
    }
}

// ─── Shimmer Loading ──────────────────────────────────────────────────────────

@Composable
private fun ShimmerContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgElev2.copy(alpha = alpha))
            )
        }
    }
}

// ─── Error Content ────────────────────────────────────────────────────────────

@Composable
private fun SmsCTABanner(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev2)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Accents.Amber.copy(alpha = 0.15f))
        ) {
            Text("💬", fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Enable SMS sync",
                fontSize = 13.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                "Auto-detect bank & UPI transactions",
                fontSize = 11.sp,
                fontFamily = InterFamily,
                color = TextTertiary
            )
        }
        TextButton(onClick = onEnable) {
            Text(
                "Enable",
                fontSize = 12.sp,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                color = Accents.Amber
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(message, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.OutlinedButton(onClick = onRetry) {
            Icon(Icons.Outlined.Refresh, "Retry", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
