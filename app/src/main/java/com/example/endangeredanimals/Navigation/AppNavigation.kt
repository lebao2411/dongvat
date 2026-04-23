package com.example.endangeredanimals.Navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
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
        modifier = modifier,

        // Hiệu ứng khi MỞ một màn hình MỚI (Trượt từ Phải sang Trái + Hiện rõ dần)
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        // Hiệu ứng của màn hình CŨ khi bị màn hình mới đè lên (Trượt sang Trái + Mờ dần)
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        // Hiệu ứng khi bấm BACK quay lại màn hình CŨ (Trượt từ Trái sang Phải + Hiện rõ dần)
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        // Hiệu ứng của màn hình HIỆN TẠI khi bị đóng đi bằng nút BACK (Trượt sang Phải + Mờ dần)
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
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

        composable("scan") {
            ScannerScreen(navController = navController)
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
