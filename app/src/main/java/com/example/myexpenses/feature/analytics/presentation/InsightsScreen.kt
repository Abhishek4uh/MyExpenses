package com.example.myexpenses.feature.analytics.presentation

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
import com.example.myexpenses.feature.analytics.presentation.components.BreakdownCard
import com.example.myexpenses.feature.analytics.presentation.components.IncomeExpenseCard
import com.example.myexpenses.feature.analytics.presentation.components.InsightCardStack
import com.example.myexpenses.feature.analytics.presentation.components.TimeFilter
import com.example.myexpenses.feature.analytics.presentation.components.TrendCard
import com.example.myexpenses.feature.main.presentation.BottomNavBarReservedHeight

/**
 * Insights V2 — six decision-focused sections:
 *  1. Header        ("INSIGHTS" eyebrow + serif title)
 *  2. Time filter   (Week / Month / Year + Custom range)
 *  3. Insights      (top 2 smart observations from InsightEngine)
 *  4. Trend chart   (smooth Catmull-Rom area chart with peak dot)
 *  5. Breakdown     (donut + legend, tap → CategoryDetail)
 *  6. Income vs Exp (two animated horizontal bars)
 */
@Composable
fun InsightsScreen(
    onNavigateToCategoryDetail: (String) -> Unit = {},
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomBarInset = BottomNavBarReservedHeight +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = bottomBarInset + 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Header() }
        item {
            TimeFilter(
                range = state.range,
                onRangeChange = viewModel::onRangeChange,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
        if (state.showInsights) {
            item {
                InsightCardStack(
                    insights = state.insights,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
        item {
            TrendCard(
                trend = state.trend,
                totalLabel = "₹${"%,d".format(state.totalSpend)}",
                delta = state.trendDelta,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
        item {
            BreakdownCard(
                breakdown = state.breakdown,
                onCategoryClick = onNavigateToCategoryDetail,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
        if (state.showIncomeExpense) {
            item {
                IncomeExpenseCard(
                    data = state.incomeExpense,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            "INSIGHTS",
            fontSize = 12.sp,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            color = Accents.Amber,
            letterSpacing = 1.4.sp,
        )
        Text(
            "Where your money\ngoes.",
            fontSize = 36.sp,
            fontFamily = SerifFamily,
            color = TextPrimary,
            lineHeight = 38.sp,
            letterSpacing = (-0.6).sp,
        )
    }
}
