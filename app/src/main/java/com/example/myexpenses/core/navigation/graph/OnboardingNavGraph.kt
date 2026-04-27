package com.example.myexpenses.core.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.example.myexpenses.core.navigation.route.Graph
import com.example.myexpenses.core.navigation.route.Onboarding
import com.example.myexpenses.feature.onboarding.presentation.OnboardingScreen

fun NavGraphBuilder.onboardingGraph(navController: NavHostController) {
    navigation(
        route = Graph.Onboarding.route,
        startDestination = Onboarding.Welcome.route){
        composable(
            route = Onboarding.Welcome.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = Onboarding.Welcome.deepLink
            })){
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate(Graph.Auth.route){
                        popUpTo(Graph.Onboarding.route){
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
