package com.example.myexpenses.feature.main.presentation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.myexpenses.core.navigation.graph.aboutTabGraph
import com.example.myexpenses.core.navigation.graph.analyticsTabGraph
import com.example.myexpenses.core.navigation.graph.homeTabGraph
import com.example.myexpenses.core.navigation.graph.settingsTabGraph
import com.example.myexpenses.core.navigation.ext.navigateToDetail
import com.example.myexpenses.core.navigation.ext.navigateToTab
import com.example.myexpenses.core.navigation.route.Graph
import com.example.myexpenses.core.navigation.route.TopLevel
import com.example.myexpenses.core.ui.theme.AppColors
import com.example.myexpenses.core.ui.theme.BgBase

/**
 * Owns the inner NavController + BottomNavBar.
 * Detail navigation always goes via rootNavController, pushing the detail onto
 * the ROOT back stack — which removes MainScreen from composition entirely and
 * makes the BottomBar disappear structurally (no show/hide hacks needed).
 */
@Composable
fun MainScreen(rootNavController: NavHostController) {

    val innerNavController = rememberNavController()

    // Box overlay: glass nav bar floats *above* the content area. The
    // InnerNavHost fills the full screen height — content scrolls behind the
    // bar visually. Each screen handles its own bottom contentPadding via
    // the [BottomNavBarReservedHeight] constant + its own
    // WindowInsets.navigationBars handling so the last list item is always
    // accessible (not trapped under the bar).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        InnerNavHost(
            innerNavController = innerNavController,
            rootNavController = rootNavController,
            modifier = Modifier.fillMaxSize(),
        )
        BottomNavBar(
            navController = innerNavController,
            onTabSelected = { item -> innerNavController.navigateToTab(item) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun InnerNavHost(
    innerNavController: NavHostController,
    rootNavController: NavHostController,
    modifier: Modifier = Modifier){

    NavHost(
        navController = innerNavController,
        startDestination = Graph.HomeTab.route,
        modifier = modifier.background(BgBase),
        enterTransition = {
            fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(220))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(220))
        }){
        homeTabGraph(
            innerNavController = innerNavController,
            onNavigateToDetail = { id ->
                rootNavController.navigateToDetail(TopLevel.HomeDetail.createRoute(id))
            },
            onNavigateToAllTransactions = {
                rootNavController.navigateToDetail(TopLevel.AllTransactions.route)
            },
        )

        analyticsTabGraph(
            innerNavController = innerNavController,
            onNavigateToCategoryDetail = { groupId ->
                rootNavController.navigateToDetail(TopLevel.CategoryDetail.createRoute(groupId))
            },
        )

        settingsTabGraph(
            innerNavController = innerNavController,
            onNavigateToDetail = { key ->
                rootNavController.navigateToDetail(TopLevel.SettingsDetail.createRoute(key))
            }
        )

        aboutTabGraph(
            innerNavController = innerNavController,
            onNavigateToDetail = {
                rootNavController.navigateToDetail(TopLevel.AboutDetail.route)
            }
        )
    }
}
