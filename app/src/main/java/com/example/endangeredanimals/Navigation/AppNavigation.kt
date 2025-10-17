package com.example.endangeredanimals.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.endangeredanimals.View.*

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LogInScreen(navController = navController)
        }
        composable("signup_screen") {
            SignUpScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("game") { GameScreen() }
        composable("love") { LoveScreen() }

        composable("profile") {
            ProfileScreen(navController = navController)
        }

        val resultScreenRoute = "result_screen/{initialQuery}"
        composable(
            route = "result_screen?category={category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ResultScreen(navController = navController, category = category)
        }
    }
}
