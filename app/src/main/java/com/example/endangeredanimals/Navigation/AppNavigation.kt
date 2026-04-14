package com.example.endangeredanimals.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.endangeredanimals.View.*

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LogInScreen(navController = navController)
        }
        composable("signup_screen") {
            SignUpScreen(navController = navController)
        }

        composable("forgotpassword_screen") {
            ForgotPasswordScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("changepassword_screen") {
            ChangePasswordScreen(navController = navController)
        }

        composable(route = "favorite_screen") {
            FavoriteScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController)
        }

        composable(
            route = "animal_screen/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId")
            if (animalId != null) {
                AnimalScreen(animalId = animalId, navController = navController)
            }
        }

        composable("result_screen") {
            ResultScreen(navController = navController)
        }
    }
}
