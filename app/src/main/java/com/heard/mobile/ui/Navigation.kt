package com.heard.mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.heard.mobile.ui.screens.AddPathScreen
import com.heard.mobile.ui.screens.HomeScreen
import com.heard.mobile.ui.screens.PathScreen
import com.heard.mobile.ui.screens.PersonalProfile
import com.heard.mobile.ui.screens.SettingsScreen
import com.heard.mobile.ui.screens.PathDetailScreen
import kotlinx.serialization.Serializable

sealed interface HeardRoute {
    @Serializable data object Home : HeardRoute
    @Serializable data class TravelDetails(val travelId: String) : HeardRoute
    @Serializable data object AddTravel : HeardRoute
    @Serializable data object Settings : HeardRoute
    @Serializable data object Profile : HeardRoute
    @Serializable data object Path : HeardRoute

}

@Composable
fun ApplicationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = HeardRoute.Home
    ) {
        composable<HeardRoute.Home> {
            HomeScreen(navController)
        }
        composable<HeardRoute.TravelDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<HeardRoute.TravelDetails>()
            PathDetailScreen(navController, route.travelId)
        }
        composable<HeardRoute.AddTravel> {
            AddPathScreen(navController)
        }
        composable<HeardRoute.Settings> {
            SettingsScreen(navController)
        }
        composable<HeardRoute.Profile> {
            PersonalProfile(navController)
        }
        composable<HeardRoute.Path> {
            PathScreen(navController)
        }
    }
}
