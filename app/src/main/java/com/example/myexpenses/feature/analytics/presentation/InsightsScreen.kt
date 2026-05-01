package com.example.myexpenses.feature.analytics.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.feature.analytics.domain.InsightsRange
import com.example.myexpenses.feature.analytics.presentation.components.BreakdownCard
import com.example.myexpenses.feature.analytics.presentation.components.GroupedBarChart
import com.example.myexpenses.feature.analytics.presentation.components.IncomeExpenseCard
import com.example.myexpenses.feature.analytics.presentation.components.IncomeExpenseLineChart
import com.example.myexpenses.feature.analytics.presentation.components.InsightCardStack
import com.example.myexpenses.feature.analytics.presentation.components.PeriodNavigator
import com.example.myexpenses.feature.analytics.presentation.components.TimeFilter
import com.example.myexpenses.feature.main.presentation.BottomNavBarReservedHeight

/**
 * Insights V3 — premium fintech layout:
 *
 *  1. Header          "INSIGHTS" eyebrow + serif title
 *  2. TimeFilter      Week / Month / Year / Custom tabs
 *  3. PeriodNavigator < 18-Apr-2026 – 24-Apr-2026 > with back/forward constraints
 *  4. InsightSummary  Smart observations from InsightEngine (when ≥ 3 transactions)
 *  5. Chart           GroupedBarChart (Week) OR IncomeExpenseLineChart (Month/Year)
 *  6. BreakdownCard   Centered donut + tappable category list with amounts
 *  7. IncomeExpenseCard  Horizontal bars showing savings rate
 */
@Composable
fun InsightsScreen(
    onNavigateToCategoryDetail: (String) -> Unit = {},
    viewModel: InsightsViewModel = hiltViewModel() ) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomBarInset = BottomNavBarReservedHeight +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Track navigation direction for slide animation
    val isWeek = state.range is InsightsRange.Week

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding(),
        contentPadding   = PaddingValues(top = 12.dp, bottom = bottomBarInset + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── 1. Header ──────────────────────────────────────────────────────────
        item {
            Header(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
        }

        // ── 2. Time tabs ───────────────────────────────────────────────────────
        item {
            TimeFilter(
                range         = state.range,
                onRangeChange = viewModel::onRangeChange,
                modifier      = Modifier.padding(horizontal = 24.dp),
            )
        }

        // ── 3. Period navigator ────────────────────────────────────────────────
        // Hidden for Custom range (user edits dates directly in the picker)
        if (state.range !is InsightsRange.Custom) {
            item {
                PeriodNavigator(
                    label        = state.periodLabel,
                    canGoBack    = state.canGoBack,
                    canGoForward = state.canGoForward,
                    onBack       = { viewModel.navigatePeriod(forward = false) },
                    onForward    = { viewModel.navigatePeriod(forward = true) },
                    modifier     = Modifier.padding(horizontal = 24.dp),
                )
            }
        }

        // ── 4. Insight summary ─────────────────────────────────────────────────
        if (state.showInsights) {
            item {
                InsightCardStack(
                    insights = state.insights,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }

        // ── 5. Income vs Expense chart (tab-aware) ─────────────────────────────
        item {
            AnimatedContent(
                targetState   = isWeek,
                transitionSpec = {
                    (fadeIn(tween(250)) + slideInHorizontally { if (targetState) -40 else 40 })
                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { if (targetState) 40 else -40 })
                },
                label = "chart_switch",
                modifier = Modifier.padding(horizontal = 24.dp)) { showWeek ->
                if (showWeek) {
                    GroupedBarChart(bars = state.weekBars)
                } else {
                    val subtitle = when (val r = state.range) {
                        is InsightsRange.Month -> r.displayLabel()
                        is InsightsRange.Year  -> r.year.toString()
                        else                   -> state.periodLabel
                    }
                    IncomeExpenseLineChart(
                        incomeSeries  = state.incomeLineSeries,
                        expenseSeries = state.expenseLineSeries,
                        periodSubtitle = subtitle,
                    )
                }
            }
        }

        // ── 6. Category breakdown (donut + list) ──────────────────────────────
        item {
            BreakdownCard(
                breakdown         = state.breakdown,
                onCategoryClick   = onNavigateToCategoryDetail,
                modifier          = Modifier.padding(horizontal = 24.dp),
            )
        }

        // ── 7. Income vs Expense summary (horizontal bars) ────────────────────
        if (state.showIncomeExpense) {
            item {
                IncomeExpenseCard(
                    data     = state.incomeExpense,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            "INSIGHTS",
            fontSize      = 12.sp,
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.SemiBold,
            color         = Accents.Amber,
            letterSpacing = 1.4.sp,
        )
        Text(
            "Where your money\ngoes.",
            fontSize      = 36.sp,
            fontFamily    = SerifFamily,
            color         = TextPrimary,
            lineHeight    = 38.sp,
            letterSpacing = (-0.6).sp,
        )
    }
}
