package com.example.myexpenses.feature.home.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.ui.theme.AppColors
import com.example.myexpenses.core.voice.ParsedVoiceIntent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import com.example.myexpenses.R
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.ui.SoundPlayer
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BgElev4
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.BorderSubtle
import com.example.myexpenses.core.ui.theme.CategoryTone
import com.example.myexpenses.core.ui.theme.CategoryTones
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary

// ─── Entry point — routes to the correct mode ─────────────────────────────────

@Composable
fun HomeDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit
) {
    when (transactionId) {
        "voice" -> VoiceEntryScreen(onNavigateBack = onNavigateBack)
        "new"   -> ManualEntryScreen(onNavigateBack = onNavigateBack)
        else    -> TransactionDetailScreen(transactionId = transactionId, onNavigateBack = onNavigateBack)
    }
}

// ─── Voice Entry ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val voiceState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Track permission state so the press-and-hold gesture knows whether
    // to start listening or trigger the system permission prompt.
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        // Don't auto-start on grant — user has to press-and-hold deliberately.
    }

    LaunchedEffect(voiceState) {
        if (voiceState == VoiceUiState.Saved) {
            SoundPlayer.playOnce(context, R.raw.faaaa)
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voice Entry",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.reset(); onNavigateBack() }) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            MicButton(
                isListening = voiceState is VoiceUiState.Listening || voiceState is VoiceUiState.Partial,
                hasPermission = hasMicPermission,
                onPressStart = { viewModel.startListening() },
                onRelease = { viewModel.stopListening() },
                onCancel = { viewModel.reset() },
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            )

            AnimatedContent(
                targetState = voiceState,
                transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
                label = "voice_status"
            ) { state ->
                Text(
                    text = when (state) {
                        is VoiceUiState.Idle        -> "Hold the mic to speak"
                        is VoiceUiState.Listening   -> "Listening… release to send"
                        is VoiceUiState.Partial     -> "\"${state.text}\""
                        is VoiceUiState.Recognized  -> "Got it!"
                        is VoiceUiState.SpeechError -> state.message
                        is VoiceUiState.Saved       -> "Saved!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (state) {
                        is VoiceUiState.Listening   -> AppColors.Primary
                        is VoiceUiState.SpeechError -> AppColors.Expense
                        else -> AppColors.TextSecondary
                    },
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(visible = voiceState is VoiceUiState.Listening) {
                WaveformBars()
            }

            if (voiceState is VoiceUiState.Partial) {
                TranscriptCard((voiceState as VoiceUiState.Partial).text)
            }

            if (voiceState is VoiceUiState.Recognized) {
                val recognized = voiceState as VoiceUiState.Recognized
                TranscriptCard(recognized.rawText)
                Spacer(Modifier.height(4.dp))
                ParsedPreviewCard(recognized.parsed)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                    Button(
                        onClick = { viewModel.saveVoiceTransaction {} },
                        enabled = recognized.parsed.amount != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor = AppColors.Background,
                            disabledContainerColor = AppColors.PrimaryContainer,
                            disabledContentColor = AppColors.TextDisabled
                        )
                    ) {
                        Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (voiceState is VoiceUiState.Idle || voiceState is VoiceUiState.SpeechError) {
                Text(
                    text = "Try: \"Spent 500 on food\" or \"Received 20000 salary\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextDisabled,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * WhatsApp-style press-and-hold microphone button.
 *
 *  • Press down → onPressStart() (start listening; permission must already be granted)
 *  • Release   → onRelease() (finalize and process the captured speech)
 *  • Cancelled (parent intercepted gesture) → onCancel()
 *
 * The button scales up while held and shows a pulsing halo. If permission is
 * not granted [onRequestPermission] is invoked instead and the gesture is no-op.
 */
@Composable
private fun MicButton(
    isListening: Boolean,
    hasPermission: Boolean,
    onPressStart: () -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    // Press-state scale — the whole button bumps up when finger is down.
    var pressed by remember { mutableStateOf(false) }
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 1.15f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "press_scale"
    )

    Box(contentAlignment = Alignment.Center) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(AppColors.Primary.copy(alpha = 0.18f))
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .scale(pressScale)
                .clip(CircleShape)
                .background(if (isListening) AppColors.Primary else AppColors.PrimaryContainer)
                .then(
                    if (!isListening) Modifier.border(1.dp, AppColors.Border, CircleShape)
                    else Modifier
                )
                .pointerInput(hasPermission) {
                    detectTapGestures(
                        onPress = {
                            if (!hasPermission) {
                                onRequestPermission()
                                return@detectTapGestures
                            }
                            pressed = true
                            onPressStart()
                            // Wait for release; tryAwaitRelease returns false
                            // if the gesture was cancelled (e.g. parent took over).
                            val released = tryAwaitRelease()
                            pressed = false
                            if (released) onRelease() else onCancel()
                        }
                    )
                }
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Hold to record",
                tint = if (isListening) AppColors.Background else AppColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun TranscriptCard(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "\"$text\"",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun ParsedPreviewCard(parsed: ParsedVoiceIntent) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Parsed as", style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                parsed.amount?.let { amount ->
                    val amountColor = if (parsed.type == TransactionType.INCOME) AppColors.Income else AppColors.Expense
                    ParsedChip(label = "₹${String.format("%.0f", amount)}", color = amountColor)
                } ?: ParsedChip(label = "Amount?", color = AppColors.Expense)

                parsed.type?.let { type ->
                    ParsedChip(label = type.name.lowercase().replaceFirstChar { it.uppercase() }, color = AppColors.Primary)
                }

                parsed.category?.let { cat ->
                    ParsedChip(label = "${cat.emoji} ${cat.displayName}", color = AppColors.TextSecondary)
                }
            }
            if (parsed.amount == null) {
                Text(
                    "No amount detected — say something like \"spent 200\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Warning
                )
            }
        }
    }
}

@Composable
private fun ParsedChip(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun WaveformBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 6f,
                targetValue = if (index % 2 == 0) 36f else 24f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + index * 60, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(AppColors.Primary, AppColors.PrimaryDim)))
            )
        }
    }
}

// ─── Manual Entry ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Transaction",
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
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Amount
            OutlinedTextField(
                value = viewModel.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                leadingIcon = {
                    Text(
                        "₹",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.Primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = entryTextFieldColors(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            // Income / Expense toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.SurfaceCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypeButton(
                        label = "Expense",
                        selected = viewModel.selectedType == TransactionType.EXPENSE,
                        selectedColor = AppColors.Expense,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) }
                    )
                    TypeButton(
                        label = "Income",
                        selected = viewModel.selectedType == TransactionType.INCOME,
                        selectedColor = AppColors.Income,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onTypeChange(TransactionType.INCOME) }
                    )
                }
            }

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                val categories = if (viewModel.selectedType == TransactionType.INCOME) {
                    listOf(ExpenseCategory.SALARY, ExpenseCategory.FREELANCE, ExpenseCategory.POCKET_MONEY, ExpenseCategory.DIVIDEND, ExpenseCategory.CASHBACK, ExpenseCategory.CREDIT_INTEREST, ExpenseCategory.OTHER_INCOME)
                } else {
                    ExpenseCategory.entries.filter {
                        it !in listOf(ExpenseCategory.SALARY, ExpenseCategory.FREELANCE, ExpenseCategory.POCKET_MONEY, ExpenseCategory.DIVIDEND, ExpenseCategory.CASHBACK, ExpenseCategory.CREDIT_INTEREST, ExpenseCategory.OTHER_INCOME)
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        CategoryChip(
                            category = category,
                            selected = viewModel.selectedCategory == category,
                            onClick = { viewModel.onCategoryChange(category) }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = viewModel.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = entryTextFieldColors()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.save {
                        SoundPlayer.playOnce(context, R.raw.faaaa);
                        onNavigateBack()
                    }
                },
                enabled = viewModel.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = AppColors.Background,
                    disabledContainerColor = AppColors.PrimaryContainer,
                    disabledContentColor = AppColors.TextDisabled
                )){
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TypeButton(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) selectedColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) selectedColor else AppColors.TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryChip(
    category: ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        AppColors.Primary
    }
    // Single shape source-of-truth so background fill, border outline, and
    // click ripple all clip to the same rounded pill — no rough edges where
    // the ripple bleeds past the rounded corners.
    val chipShape = RoundedCornerShape(50)
    Surface(
        onClick = onClick,
        shape = chipShape,
        color = if (selected) accentColor.copy(alpha = 0.18f) else AppColors.SurfaceCard,
        // Border passed via Surface's parameter (not as a Modifier) so it's
        // drawn inside the same graphics layer as the ripple and clipped
        // correctly to chipShape.
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else AppColors.Border
        )
    ) {
        Text(
            text = "${category.emoji} ${category.displayName}",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accentColor else AppColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun entryTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.Primary,
    unfocusedBorderColor = AppColors.Border,
    focusedLabelColor = AppColors.Primary,
    unfocusedLabelColor = AppColors.TextSecondary,
    cursorColor = AppColors.Primary,
    focusedTextColor = AppColors.TextPrimary,
    unfocusedTextColor = AppColors.TextPrimary
)

// ─── Transaction Detail (existing transaction) ────────────────────────────────

@Composable
private fun TransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val transaction by viewModel.getTransactionById(transactionId).collectAsStateWithLifecycle(null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = BgElev1,
            title = { Text("Delete Transaction?", color = TextPrimary, fontFamily = InterFamily) },
            text = { Text("This action cannot be undone.", color = TextTertiary, fontFamily = InterFamily) },
            confirmButton = {
                TextButton(onClick = {
                    transaction?.let { viewModel.deleteTransaction(it.id) }
                    onNavigateBack()
                }) { Text("Delete", color = Danger, fontFamily = InterFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextTertiary, fontFamily = InterFamily)
                }
            }
        )
    }

    if (showEditDialog) {
        transaction?.let { tx ->
            EditTransactionDialog(
                initial = tx,
                onDismiss = { showEditDialog = false },
                onSave = { updated ->
                    viewModel.updateTransaction(updated)
                    showEditDialog = false
                }
            )
        }
    }

    val tx = transaction ?: run {
        Box(Modifier.fillMaxSize().background(BgBase))
        return
    }

    val tone       = categoryToneForDetail(tx.category)
    val sign       = if (tx.type == TransactionType.INCOME) "+" else "−"
    val amountStr  = String.format("%.0f", tx.amount)
    val dateStr    = tx.dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    val timeStr    = tx.dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Hero ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(tone.bg.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BgBase.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(tone.bg)
                    ) {
                        Text(tx.category.emoji, fontSize = 26.sp)
                    }
                    Text(
                        tx.category.displayName,
                        fontSize = 12.sp,
                        fontFamily = InterFamily,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "$sign₹",
                            fontFamily = SerifFamily,
                            fontSize = 22.sp,
                            fontStyle = FontStyle.Italic,
                            color = tone.fg,
                            modifier = Modifier.padding(bottom = 10.dp, end = 2.dp)
                        )
                        Text(
                            amountStr,
                            fontFamily = InterFamily,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            letterSpacing = (-1.5).sp
                        )
                    }
                    Text(
                        "$dateStr · $timeStr",
                        fontSize = 12.sp,
                        fontFamily = InterFamily,
                        color = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Detail rows ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BgElev1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            ) {
                DetailRow(label = "CATEGORY", value = "${tx.category.emoji}  ${tx.category.displayName}", icon = "📂")
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                DetailRow(label = "NOTE", value = tx.note.ifBlank { "—" }, icon = "📝")
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                DetailRow(
                    label = "SOURCE",
                    value = when (tx.source) {
                        EntrySource.MANUAL -> "Manual entry"
                        EntrySource.VOICE  -> "Voice entry"
                        EntrySource.SMS    -> "Auto-read from SMS"
                    },
                    icon = when (tx.source) {
                        EntrySource.MANUAL -> "✏️"
                        EntrySource.VOICE  -> "🎙️"
                        EntrySource.SMS    -> "📩"
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // ─── Actions ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgElev3)
                        .border(1.dp, BorderDefault, RoundedCornerShape(14.dp))
                        .clickable { showEditDialog = true }
                ) {
                    Text(
                        "Edit",
                        fontSize = 14.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Danger.copy(alpha = 0.12f))
                        .border(1.dp, Danger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { showDeleteDialog = true }
                ) {
                    Text(
                        "Delete",
                        fontSize = 14.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Medium,
                        color = Danger
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgElev4)
        ) {
            Text(icon, fontSize = 15.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 10.sp,
                fontFamily = InterFamily,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontFamily = InterFamily,
                color = TextPrimary
            )
        }
    }
}

private fun categoryToneForDetail(category: ExpenseCategory): CategoryTone =
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

// ─── Edit Transaction Dialog ───────────────────────────────────────────────────

@Composable
private fun EditTransactionDialog(
    initial: com.example.myexpenses.core.common.Transaction,
    onDismiss: () -> Unit,
    onSave: (com.example.myexpenses.core.common.Transaction) -> Unit
) {
    var amountText by remember { mutableStateOf(String.format("%.0f", initial.amount)) }
    var note by remember { mutableStateOf(initial.note) }
    var selectedCategory by remember { mutableStateOf(initial.category) }
    val isIncome = initial.type == TransactionType.INCOME
    val categories = remember(isIncome) {
        if (isIncome) listOf(ExpenseCategory.SALARY, ExpenseCategory.FREELANCE, ExpenseCategory.POCKET_MONEY, ExpenseCategory.DIVIDEND, ExpenseCategory.CASHBACK, ExpenseCategory.CREDIT_INTEREST, ExpenseCategory.OTHER_INCOME)
        else ExpenseCategory.entries.filter {
            it !in listOf(ExpenseCategory.SALARY, ExpenseCategory.FREELANCE, ExpenseCategory.POCKET_MONEY, ExpenseCategory.DIVIDEND, ExpenseCategory.CASHBACK, ExpenseCategory.CREDIT_INTEREST, ExpenseCategory.OTHER_INCOME)
        }
    }
    val canSave = amountText.toDoubleOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElev1,
        title = { Text("Edit Transaction", color = TextPrimary, fontFamily = InterFamily, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.matches(Regex("""^\d{0,10}(\.\d{0,2})?$"""))) {
                            amountText = v
                        }
                    },
                    label = { Text("Amount", fontFamily = InterFamily) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary,
                        unfocusedLabelColor = AppColors.TextSecondary,
                        cursorColor = AppColors.Primary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Category chips — FlowRow wraps to multiple lines so chips
                // never overflow the dialog width and never get clipped.
                Text("Category", fontSize = 11.sp, fontFamily = InterFamily, color = TextMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val accent = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            AppColors.Primary
                        }
                        val isSelected = selectedCategory == cat
                        // Single shape source-of-truth so border + clip + ripple all match.
                        val chipShape = RoundedCornerShape(50)
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = chipShape,
                            color = if (isSelected) accent.copy(alpha = 0.18f) else AppColors.SurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accent else AppColors.Border
                            )
                        ) {
                            Text(
                                text = "${cat.emoji} ${cat.displayName}",
                                fontSize = 11.sp,
                                fontFamily = InterFamily,
                                color = if (isSelected) accent else AppColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(120) },
                    label = { Text("Note", fontFamily = InterFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary,
                        unfocusedLabelColor = AppColors.TextSecondary,
                        cursorColor = AppColors.Primary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newAmount = amountText.toDoubleOrNull() ?: return@Button
                    onSave(
                        initial.copy(
                            amount = newAmount,
                            category = selectedCategory,
                            note = note.trim()
                        )
                    )
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary, contentColor = BgBase),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Save", fontFamily = InterFamily) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted, fontFamily = InterFamily)
            }
        }
    )
}
