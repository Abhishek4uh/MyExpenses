package com.example.myexpenses.core.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.example.myexpenses.core.navigation.route.Auth
import com.example.myexpenses.core.navigation.route.Graph
import com.example.myexpenses.feature.auth.presentation.RegisterNameScreen

/**
 * AUTH NESTED GRAPH
 * ─────────────────────────────────────────────────────────────────────────────
 * Currently has one screen: RegisterName.
 * Easy to extend: add Login, OTP, ForgotPassword as additional composables
 * inside this navigation() block.
 *
 * Exit: register → main_graph (pops entire auth graph so back press
 * from main app cannot return to registration).
 * ─────────────────────────────────────────────────────────────────────────────
 */
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        route = Graph.Auth.route,
        startDestination = Auth.RegisterName.route){
        composable(
            route = Auth.RegisterName.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = Auth.RegisterName.deepLink
            })){
            RegisterNameScreen(
                onRegistrationComplete = {
                    navController.navigate(Graph.Main.route){
                        popUpTo(Graph.Auth.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
