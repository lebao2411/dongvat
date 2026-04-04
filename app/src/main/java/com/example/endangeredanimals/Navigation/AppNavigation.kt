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

        composable("inden_game") {
            IndenGameScreen(navController = navController)
        }

        composable("changepassword_screen") {
            ChangePasswordScreen(navController = navController)
        }
        
        composable("game") { 
            GameScreen(navController = navController) 
        }

        composable("story_selection") {
            StoryGameScreen(navController = navController)
        }

        // Khai báo chuẩn xác Route cho chơi game
        composable(
            route = "story_play/{gameId}",
            arguments = listOf(
                navArgument("gameId") { 
                    type = NavType.LongType 
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            
            // Bạn cần tạo file StoryPlayScreen.kt
            // StoryPlayScreen(gameId = gameId, navController = navController)
            
            // Tạm thời hiển thị màn hình chờ để không bị crash
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Đang vào màn chơi ID: $gameId")
            }
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
