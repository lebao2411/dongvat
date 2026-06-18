package com.example.endangeredanimals.Navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.endangeredanimals.View.*
import com.example.endangeredanimals.ViewModel.AdminViewModel
import com.example.endangeredanimals.ViewModel.NotificationViewModel

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier, startDestination: String) {

    val sharedNotificationViewModel: NotificationViewModel = viewModel()
    val sharedAdminViewModel: AdminViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("main_screen") { MainScreen(rootNavController = navController) }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role == "admin") "admin_management" else "main_screen"
                    navController.navigate(destination) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToForgotPassword = { navController.navigate("forgotpassword") }
            )
        }

        composable("signup") { SignUpScreen(navController = navController) }
        composable("forgotpassword") { ForgotPasswordScreen(navController = navController) }
        composable("changepassword") { ChangePasswordScreen(navController = navController) }
        composable("profile") { ProfileScreen(navController = navController) }
        composable("favorite") { FavoriteScreen(navController = navController) }

        composable("contribution") {
            ContributeScreen(navController = navController, initialImageUri = null, aiSpeciesResult = null)
        }

        composable(
            route = "contribute_screen/{imageUri}/{aiResult}",
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("aiResult") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val encodedAiResult = backStackEntry.arguments?.getString("aiResult") ?: ""
            val decodedUri = Uri.decode(encodedUri)
            val decodedAiResult = Uri.decode(encodedAiResult)

            ContributeScreen(
                navController = navController,
                initialImageUri = if (decodedUri.isNotBlank() && decodedUri != "null") Uri.parse(decodedUri) else null,
                aiSpeciesResult = if (decodedAiResult.isNotBlank() && decodedAiResult != "null") decodedAiResult else null
            )
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

        // ĐÃ THÊM: Route tái sử dụng DiscussScreen, truyền ID để tự động mở BottomSheet
        composable(
            route = "discuss_detail/{contributionId}",
            arguments = listOf(navArgument("contributionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contributionId = backStackEntry.arguments?.getString("contributionId")
            DiscussScreen(navController = navController, initialContributionId = contributionId)
        }

        composable("result_screen") { ResultScreen(navController = navController) }
        composable("leaderboard") { LeaderboardScreen(navController = navController) }

        composable("notifications") {
            NotificationScreen(navController = navController, viewModel = sharedNotificationViewModel)
        }

        composable("admin_management") {
            AdminManagementScreen(rootController = navController, viewModel = sharedAdminViewModel)
        }

        composable(
            route = "manage_contribution/{contributionId}",
            arguments = listOf(navArgument("contributionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contributionId = backStackEntry.arguments?.getString("contributionId") ?: ""
            ManageContributionScreen(
                navController = navController,
                contributionId = contributionId,
                viewModel = sharedAdminViewModel
            )
        }
    }
}