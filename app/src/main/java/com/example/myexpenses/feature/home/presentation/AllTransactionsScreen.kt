package com.example.myexpenses.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev2
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BgElev5
import com.example.myexpenses.core.ui.theme.Danger
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.Success
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (transactionId: String) -> Unit,
    viewModel: AllTransactionsViewModel = hiltViewModel(),
) {
    val expenseState by viewModel.expenseState.collectAsStateWithLifecycle()
    val incomeState by viewModel.incomeState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        AllTransactionsHeader(
            onNavigateBack = onNavigateBack,
            expenseCount = expenseState.totalAvailable,
            incomeCount = incomeState.totalAvailable,
        )

        TransactionsTabRow(
            selectedIndex = pagerState.currentPage,
            onSelect = { idx ->
                coroutineScope.launch { pagerState.animateScrollToPage(idx) }
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Allow children to handle their own scrolling without conflict
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
                0 -> TransactionsTab(
                    state = expenseState,
                    onLoadMore = viewModel::loadMoreExpense,
                    onItemClick = onNavigateToDetail,
                    emptyMessage = "No expenses yet",
                    emptyHint = "Tap + on Home to log one.",
                )
                1 -> TransactionsTab(
                    state = incomeState,
                    onLoadMore = viewModel::loadMoreIncome,
                    onItemClick = onNavigateToDetail,
                    emptyMessage = "No income yet",
                    emptyHint = "Add income manually from Home.",
                )
            }
        }
    }
}

// ─── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun AllTransactionsHeader(
    onNavigateBack: () -> Unit,
    expenseCount: Int,
    incomeCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BgElev2)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.size(Spacing.sm))
        Column {
            Text(
                "Transactions",
                fontFamily = SerifFamily,
                fontSize = 22.sp,
                color = TextPrimary
            )
            val total = expenseCount + incomeCount
            if (total > 0) {
                Text(
                    "$expenseCount expense • $incomeCount income",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// ─── Tab row (segmented control) ───────────────────────────────────────────────

@Composable
private fun TransactionsTabRow(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF101010))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Expense", "Income").forEachIndexed { index, label ->
            val isSelected = selectedIndex == index
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) BgElev5 else Color.Transparent,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tx_tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextPrimary else TextMuted,
                animationSpec = tween(220),
                label = "tx_tab_text"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bgColor)
                    .clickable { onSelect(index) }
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontFamily = InterFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

// ─── Per-tab list ──────────────────────────────────────────────────────────────

@Composable
private fun TransactionsTab(
    state: TransactionsTabState,
    onLoadMore: () -> Unit,
    onItemClick: (String) -> Unit,
    emptyMessage: String,
    emptyHint: String,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val total = layout.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= total - 4
        }
    }

    LaunchedEffect(shouldLoadMore, state.hasMore, state.isInitialLoading) {
        if (shouldLoadMore && state.hasMore && !state.isLoadingMore && !state.isInitialLoading) {
            onLoadMore()
        }
    }

    when {
        state.isInitialLoading -> ShimmerList()
        state.transactions.isEmpty() -> EmptyState(emptyMessage, emptyHint)
        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.xxl,
                end = Spacing.xxl,
                top = Spacing.xs,
                // Generous bottom padding so the last row clears the system
                // navigation bar and feels comfortable to read.
                bottom = Spacing.huge,
            )
        ) {
            items(
                items = state.transactions,
                key = { it.id }
            ) { tx ->
                AllTransactionRow(
                    transaction = tx,
                    onClick = { onItemClick(tx.id) },
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            if (state.isLoadingMore) {
                item { LoadingMoreFooter() }
            } else if (!state.hasMore && state.transactions.isNotEmpty()) {
                item { EndOfListFooter(state.transactions.size) }
            }
        }
    }
}

@Composable
private fun AllTransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val sign = if (isIncome) "+" else "−"
    val amountColor = if (isIncome) Success else Danger
    val dateStr = transaction.dateTime.format(DateTimeFormatter.ofPattern("d MMM • h:mm a"))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev1)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BgElev3)
        ) {
            Text(transaction.category.emoji, fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = transaction.category.displayName,
                fontFamily = InterFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = if (transaction.note.isBlank()) dateStr else "${transaction.note} • $dateStr",
                fontFamily = InterFamily,
                fontSize = 11.sp,
                color = TextTertiary,
                maxLines = 1
            )
        }
        Text(
            text = "$sign₹${String.format("%,.0f", transaction.amount)}",
            fontFamily = InterFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

// ─── Shimmer (initial load) ────────────────────────────────────────────────────

@Composable
private fun ShimmerList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.xxl,
            end = Spacing.xxl,
            top = Spacing.xs,
            bottom = Spacing.huge,
        )
    ) {
        items(8) {
            ShimmerRow(modifier = Modifier.padding(vertical = 3.dp))
        }
    }
}

@Composable
private fun ShimmerRow(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    val shimmerColor = BgElev3.copy(alpha = alpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev1)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerColor)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor)
            )
        }
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
    }
}

// ─── Bottom-of-list footers ────────────────────────────────────────────────────

@Composable
private fun LoadingMoreFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            color = Accents.Amber,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(
            "Loading more…",
            fontFamily = InterFamily,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun EndOfListFooter(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "All $count transactions shown",
            fontFamily = InterFamily,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun EmptyState(message: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📭", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            hint,
            fontFamily = InterFamily,
            fontSize = 13.sp,
            color = TextTertiary
        )
    }
}
