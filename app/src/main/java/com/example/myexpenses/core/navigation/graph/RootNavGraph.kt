package com.example.myexpenses.core.navigation.graph

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.navigation.route.Args
import com.example.myexpenses.core.navigation.route.Graph
import com.example.myexpenses.core.navigation.route.TopLevel
import com.example.myexpenses.feature.about.presentation.AboutDetailScreen
import com.example.myexpenses.feature.analytics.presentation.CategoryDetailScreen
import com.example.myexpenses.feature.home.presentation.AllTransactionsScreen
import com.example.myexpenses.feature.home.presentation.HomeDetailScreen
import com.example.myexpenses.feature.main.presentation.MainScreen
import com.example.myexpenses.feature.settings.presentation.SettingsDetailScreen
import com.example.myexpenses.feature.splash.presentation.SplashScreen

/**
 * ROOT NAV HOST
 * ─────────────────────────────────────────────────────────────────────────────
 * This is the outermost NavHost. It owns:
 *   • Splash
 *   • Onboarding graph  (nested)
 *   • Auth graph        (nested)
 *   • Main destination  → renders MainScreen (which has its own inner NavHost)
 *   • All detail screens (registered here so they're outside MainScreen's Scaffold)
 *
 * IMPORTANT: Detail screens MUST be here, not inside MainScreen's NavHost.
 * By being at the root, when navigated to they replace the entire MainScreen
 * composable — the BottomBar doesn't exist. Structurally absent, not hidden.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: String = TopLevel.Splash.route){
    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = "root",
        modifier = Modifier.fillMaxSize().background(BgBase),
        enterTransition    = { fadeIn(tween(280, easing = FastOutSlowInEasing))},
        exitTransition     = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(280, easing = FastOutSlowInEasing))},
        popExitTransition  = { fadeOut(tween(200))}){

        // ── Splash ────────────────────────────────────────────────────────────
        composable(
            route = TopLevel.Splash.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = TopLevel.Splash.deepLink
            })){
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Graph.Onboarding.route) {
                        popUpTo(TopLevel.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Graph.Main.route) {
                        popUpTo(TopLevel.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding nested graph ───────────────────────────────────────────
        onboardingGraph(
            navController = navController
        )

        // ── Auth nested graph ─────────────────────────────────────────────────
        authGraph(
            navController = navController
        )

        // ── Main ──────────────────────────────────────────────────────────────
        composable(
            route = Graph.Main.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = TopLevel.Main.deepLink
            })){
            MainScreen(
                rootNavController = navController
            )
        }

        // ── Detail screens (ROOT level — no BottomBar by architecture) ────────

        composable(
            route = TopLevel.HomeDetail.route,
            arguments = listOf(
                navArgument(Args.TRANSACTION_ID) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = TopLevel.HomeDetail.deepLink }),
            enterTransition    = { slideInVertically(tween(380, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(280)) },
            exitTransition     = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition  = { slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250)) },
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Args.TRANSACTION_ID) ?: return@composable
            HomeDetailScreen(
                transactionId = id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = TopLevel.AllTransactions.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = TopLevel.AllTransactions.deepLink
            }),
            enterTransition = {
                slideInVertically(tween(380, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(280))
            },
            exitTransition = {
                fadeOut(tween(150))
            },
            popEnterTransition = {
                fadeIn(tween(150))
            },
            popExitTransition  = {
                slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            }){
            AllTransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { id ->
                    navController.navigate(TopLevel.HomeDetail.createRoute(id))
                }
            )
        }

        composable(
            route = TopLevel.CategoryDetail.route,
            arguments = listOf(
                navArgument(Args.GROUP_ID) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = TopLevel.CategoryDetail.deepLink }),
            enterTransition = {
                slideInVertically(tween(380, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(280))
            },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = {
                slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            },
        ) {
            CategoryDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = TopLevel.SettingsDetail.route,
            arguments = listOf(
                navArgument(Args.SETTINGS_KEY) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = TopLevel.SettingsDetail.deepLink
            })){backStackEntry ->
            val key = backStackEntry.arguments?.getString(Args.SETTINGS_KEY) ?: return@composable
            SettingsDetailScreen(
                settingsKey = key,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = TopLevel.AboutDetail.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = TopLevel.AboutDetail.deepLink
            })){
            AboutDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
