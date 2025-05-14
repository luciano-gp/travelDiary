package com.example.traveldiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.traveldiary.ui.screens.LoginScreen
import com.example.traveldiary.ui.screens.SignUpScreen
import com.example.traveldiary.ui.screens.TripFormScreen
import com.example.traveldiary.ui.screens.TripsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavRoutes.SIGNUP) {
            SignUpScreen(navController)
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(navController)
        }

        composable(NavRoutes.MAIN) {
            TripsScreen(navController)
        }

         composable(NavRoutes.TRIP_FORM) {
             TripFormScreen(navController)
         }

        composable("trip_form?tripId={tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripFormScreen(
                navController = navController,
                tripId = tripId
            )
        }
    }
}
