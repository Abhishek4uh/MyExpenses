package com.example.myexpenses.core.navigation.ext

import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import com.example.myexpenses.core.navigation.route.BottomNavItem
import com.example.myexpenses.core.navigation.route.Graph

/**
 * TAB SWITCHING — THE RIGHT WAY
 * ─────────────────────────────────────────────────────────────────────────────
 * Three concerns when switching tabs:
 *
 * 1. saveState = true    → saves the back stack of the tab we're leaving
 * 2. restoreState = true → restores the back stack of the tab we're entering
 * 3. launchSingleTop     → don't create a duplicate if already on this tab
 * 4. popUpTo(startDestination) → prevents back stack from growing unboundedly
 *    when switching tabs (without this, every tab switch adds to the stack)
 * ─────────────────────────────────────────────────────────────────────────────
 */
fun NavController.navigateToTab(item: BottomNavItem) {
    navigate(item.graph.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Safe navigation that guards against double taps causing IllegalStateException.
 */
fun NavController.navigateSafe(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (currentDestination?.route != route) {
        navigate(route, builder)
    }
}

/**
 * Navigate to a detail screen from a tab.
 * Pushes onto the ROOT back stack — detail screens are at root level,
 * so pressing back from a detail restores the full MainScreen + BottomBar.
 */
fun NavController.navigateToDetail(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Returns true if the given graph route is the currently active tab graph.
 */
fun NavController.isTabSelected(graph: Graph): Boolean {
    return try {
        val hierarchy = currentBackStackEntry
            ?.destination
            ?.hierarchy
        hierarchy?.any { it.route == graph.route } == true
    } catch (e: Exception) {
        false
    }
}
