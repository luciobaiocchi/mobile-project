package com.heard.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.heard.mobile.ui.screens.addPath.AddPathScreen
import com.heard.mobile.ui.screens.group.GroupScreen
import com.heard.mobile.ui.screens.home.HomeScreen
import com.heard.mobile.ui.screens.login.AuthViewModel
import com.heard.mobile.ui.screens.login.LoginScreen
import com.heard.mobile.ui.screens.path.PathScreen
import com.heard.mobile.ui.screens.personal.PersonalProfile
import com.heard.mobile.ui.screens.settings.SettingsScreen
import com.heard.mobile.ui.screens.pathDetail.PathDetailScreen
import com.heard.mobile.ui.screens.register.RegisterScreen
import com.heard.mobile.ui.screens.settings.PrivacyScreen
import com.heard.mobile.ui.screens.settings.SupportScreen
import com.heard.mobile.viewmodel.SettingsViewModel
import com.heard.mobile.viewmodel.ThemeViewModel
import kotlinx.serialization.Serializable

sealed interface HeardRoute {
    @Serializable data object Home : HeardRoute
    @Serializable data class TravelDetails(val travelId: String) : HeardRoute
    @Serializable data object AddTravel : HeardRoute
    @Serializable data object Settings : HeardRoute
    @Serializable data object Profile : HeardRoute
    @Serializable data object Path : HeardRoute
    @Serializable data object Login : HeardRoute
    @Serializable data object Register : HeardRoute
    @Serializable data object Group : HeardRoute
    @Serializable data object Privacy : HeardRoute
    @Serializable data object Support : HeardRoute


}

@Composable
fun ApplicationGraph(navController: NavHostController, themeViewModel: ThemeViewModel, authViewModel: AuthViewModel) {
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) HeardRoute.Home else HeardRoute.Login,
    ) {
        composable<HeardRoute.Login> {

            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(HeardRoute.Home) {
                        popUpTo(HeardRoute.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<HeardRoute.Register> {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    navController.navigate(HeardRoute.Home) {
                        popUpTo(HeardRoute.Register) { inclusive = true }
                    }
                }
            )
        }
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
            SettingsScreen(navController, themeViewModel, authViewModel, settingsViewModel)
        }
        composable<HeardRoute.Profile> {
            PersonalProfile(navController)
        }
        composable<HeardRoute.Path> {
            PathScreen(navController)
        }
        composable<HeardRoute.Group> {
            GroupScreen("tQeHQKlUHfiAzGCFxKBD",navController)
        }
        composable<HeardRoute.Privacy> {
            PrivacyScreen(navController)
        }
        composable<HeardRoute.Support> {
            SupportScreen(navController)
        }
    }
}
