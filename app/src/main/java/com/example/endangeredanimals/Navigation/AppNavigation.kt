package com.example.endangeredanimals.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.endangeredanimals.View.GameScreen
import com.example.endangeredanimals.View.HomeScreen
import com.example.endangeredanimals.View.LoveScreen
import com.example.endangeredanimals.View.ProfileScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home", // Route mặc định
        modifier = modifier
    ) {
        composable("home") { HomeScreen() }
        composable("game") { GameScreen() }
        composable("love") { LoveScreen() }
        composable("profile") { ProfileScreen() }
    }
}