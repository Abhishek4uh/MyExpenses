package com.example.myexpenses.core.navigation.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * NAVIGATION ARCHITECTURE
 * ────────────────────────────────────────────────────────────────
 * All routes live here. Zero string literals outside this file.
 *
 * Sealed interface hierarchy:
 *   NavRoute
 *   ├── Graph          → NavHost graph roots (used in nested nav)
 *   ├── TopLevel       → Screens reachable from root NavHost directly
 *   └── Tab            → Screens inside the inner (Main) NavHost
 *
 * Deep link scheme: "expensemanager://"
 * Every screen that can be externally linked has a companion DEEP_LINK.
 * ────────────────────────────────────────────────────────────────
 */

const val DEEP_LINK_SCHEME = "expensemanager"
const val DEEP_LINK_BASE   = "$DEEP_LINK_SCHEME://"

// ─── Root sealed interface ────────────────────────────────────────────────────

sealed interface NavRoute {
    val route: String
}

// ─── Graph roots ──────────────────────────────────────────────────────────────

sealed interface Graph : NavRoute {
    data object Onboarding : Graph {
        override val route = "graph/onboarding"
    }
    data object Auth : Graph {
        override val route = "graph/auth"
    }
    data object Main : Graph {
        override val route = "graph/main"
    }
    // Tab sub-graphs (inside the inner NavHost)
    data object HomeTab : Graph {
        override val route = "graph/tab/home"
    }
    data object AnalyticsTab : Graph {
        override val route = "graph/tab/analytics"
    }
    data object SettingsTab : Graph {
        override val route = "graph/tab/settings"
    }
    data object AboutTab : Graph {
        override val route = "graph/tab/about"
    }
}

// ─── Top-level root screens ───────────────────────────────────────────────────

sealed interface TopLevel : NavRoute {

    /** Animated splash — shows once, then routes to onboarding or main */
    data object Splash : TopLevel {
        override val route = "splash"
        val deepLink = "${DEEP_LINK_BASE}splash"
    }

    /** Entry composable that renders Scaffold + BottomBar + inner NavHost */
    data object Main : TopLevel {
        override val route = "main"
        val deepLink = "${DEEP_LINK_BASE}home"
    }

    // ── Detail screens at ROOT level ──────────────────────────────────────────
    // These are intentionally OUTSIDE MainScreen. When navigated to, the entire
    // MainScreen (Scaffold + BottomBar) is replaced. No show/hide needed.

    data object HomeDetail : TopLevel {
        override val route = "home/detail/{${Args.TRANSACTION_ID}}"
        val deepLink = "${DEEP_LINK_BASE}home/detail/{${Args.TRANSACTION_ID}}"

        fun createRoute(id: String) = "home/detail/$id"
        fun createDeepLink(id: String) = "${DEEP_LINK_BASE}home/detail/$id"
    }

    /** Full list of all transactions, opened from Home's "See all" CTA. */
    data object AllTransactions : TopLevel {
        override val route = "home/all"
        val deepLink = "${DEEP_LINK_BASE}home/all"
    }

    /** Drill-down from the Insights breakdown card into a single category group. */
    data object CategoryDetail : TopLevel {
        override val route = "analytics/category/{${Args.GROUP_ID}}"
        val deepLink = "${DEEP_LINK_BASE}analytics/category/{${Args.GROUP_ID}}"

        fun createRoute(groupId: String) = "analytics/category/$groupId"
    }

    data object SettingsDetail : TopLevel {
        override val route = "settings/detail/{${Args.SETTINGS_KEY}}"
        val deepLink = "${DEEP_LINK_BASE}settings/detail/{${Args.SETTINGS_KEY}}"

        fun createRoute(key: String) = "settings/detail/$key"
    }

    data object AboutDetail : TopLevel {
        override val route = "about/detail"
        val deepLink = "${DEEP_LINK_BASE}about/detail"
    }
}

// ─── Onboarding screens ───────────────────────────────────────────────────────

sealed interface Onboarding : NavRoute {
    data object Welcome : Onboarding {
        override val route = "onboarding/welcome"
        val deepLink = "${DEEP_LINK_BASE}onboarding"
    }
    data object Features : Onboarding {
        override val route = "onboarding/features"
        val deepLink = "${DEEP_LINK_BASE}onboarding/features"
    }
}

// ─── Auth screens ─────────────────────────────────────────────────────────────

sealed interface Auth : NavRoute {
    data object RegisterName : Auth {
        override val route = "auth/register"
        val deepLink = "${DEEP_LINK_BASE}auth/register"
    }
}

// ─── Tab screens (inside inner NavHost) ──────────────────────────────────────

sealed interface Tab : NavRoute {
    data object Home : Tab {
        override val route = "tab/home"
        val deepLink = "${DEEP_LINK_BASE}home"
    }
    data object Analytics : Tab {
        override val route = "tab/analytics"
        val deepLink = "${DEEP_LINK_BASE}analytics"
    }
    data object Settings : Tab {
        override val route = "tab/settings"
        val deepLink = "${DEEP_LINK_BASE}settings"
    }
    data object About : Tab {
        override val route = "tab/about"
        val deepLink = "${DEEP_LINK_BASE}about"
    }
}

// ─── Nav Argument Keys ────────────────────────────────────────────────────────

object Args {
    const val TRANSACTION_ID = "transactionId"
    const val SETTINGS_KEY   = "settingsKey"
    const val GROUP_ID       = "groupId"
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────

enum class BottomNavItem(
    val tab: Tab,
    val graph: Graph,
    val label: String,
    val icon: ImageVector){
    HOME(
        tab = Tab.Home,
        graph = Graph.HomeTab,
        label = "Home",
        icon = Icons.Rounded.Home
    ),
    ANALYTICS(
        tab = Tab.Analytics,
        graph = Graph.AnalyticsTab,
        label = "Insight",
        icon = Icons.Outlined.BarChart
    ),
    SETTINGS(
        tab = Tab.Settings,
        graph = Graph.SettingsTab,
        label = "Settings",
        icon = Icons.Rounded.Settings
    ),
    ABOUT(
        tab = Tab.About,
        graph = Graph.AboutTab,
        label = "About",
        icon = Icons.Rounded.Info
    );
}
