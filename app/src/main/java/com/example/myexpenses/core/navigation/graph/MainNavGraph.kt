package com.example.myexpenses.core.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.example.myexpenses.core.navigation.route.Graph
import com.example.myexpenses.core.navigation.route.Tab
import com.example.myexpenses.feature.about.presentation.AboutScreen
import com.example.myexpenses.feature.analytics.presentation.InsightsScreen
import com.example.myexpenses.feature.home.presentation.HomeScreen
import com.example.myexpenses.feature.settings.presentation.SettingsScreen

/**
 * INNER (MAIN) NAV GRAPHS
 * ─────────────────────────────────────────────────────────────────────────────
 * Three tab graphs registered in the INNER NavHost (inside MainScreen).
 *
 * Each graph:
 *   • Has its own route prefix → enables independent back stack per tab
 *   • Has ONE root screen (the tab content)
 *   • The root screen receives [onNavigateToDetail] callback that routes to
 *     the ROOT navController, pushing detail OUTSIDE MainScreen's Scaffold.
 *
 * Why no detail screens here?
 * Details are in the ROOT NavHost. The inner NavHost tabs only hold the
 * "list / summary" layer. This keeps tab graphs lean and ensures BottomBar
 * is structurally absent on detail screens.
 * ─────────────────────────────────────────────────────────────────────────────
 */

// ─── Home Tab Graph ───────────────────────────────────────────────────────────

fun NavGraphBuilder.homeTabGraph(
    innerNavController: NavHostController,
    onNavigateToDetail: (transactionId: String) -> Unit,
    onNavigateToAllTransactions: () -> Unit
) {
    navigation(
        route = Graph.HomeTab.route,
        startDestination = Tab.Home.route) {
        composable(
            route = Tab.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = Tab.Home.deepLink })
        ) {
            HomeScreen(
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToSettings = { innerNavController.navigate(Tab.Settings.route) },
                onNavigateToAllTransactions = onNavigateToAllTransactions
            )
        }
    }
}

// ─── Analytics Tab Graph ──────────────────────────────────────────────────────

fun NavGraphBuilder.analyticsTabGraph(
    innerNavController: NavHostController,
    onNavigateToCategoryDetail: (String) -> Unit,
) {
    navigation(
        route = Graph.AnalyticsTab.route,
        startDestination = Tab.Analytics.route
    ) {
        composable(
            route = Tab.Analytics.route,
            deepLinks = listOf(navDeepLink { uriPattern = Tab.Analytics.deepLink })
        ) {
            InsightsScreen(
                onNavigateToCategoryDetail = onNavigateToCategoryDetail,
            )
        }
    }
}

// ─── Settings Tab Graph ───────────────────────────────────────────────────────

fun NavGraphBuilder.settingsTabGraph(
    innerNavController: NavHostController,
    onNavigateToDetail: (key: String) -> Unit
) {
    navigation(
        route = Graph.SettingsTab.route,
        startDestination = Tab.Settings.route
    ) {
        composable(
            route = Tab.Settings.route,
            deepLinks = listOf(navDeepLink { uriPattern = Tab.Settings.deepLink })
        ) {
            SettingsScreen(
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

//─── About Tab Graph ──────────────────────────────────────────────────────────

fun NavGraphBuilder.aboutTabGraph(
    innerNavController: NavHostController,
    onNavigateToDetail: () -> Unit){
    navigation(
        route = Graph.AboutTab.route,
        startDestination = Tab.About.route){
        composable(
            route = Tab.About.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = Tab.About.deepLink
            })){
            AboutScreen(
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}
