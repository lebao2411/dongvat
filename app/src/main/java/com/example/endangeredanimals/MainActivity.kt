package com.example.endangeredanimals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.Navigation.AppNavigation
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.ui.EndangeredAnimalsTheme
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EndangeredAnimalsTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val client = SupabaseInstance.client
    val sessionStatus by client.auth.sessionStatus.collectAsState()

    val navController = rememberNavController()

    // Theo dõi trạng thái đăng nhập để điều hướng tự động
    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                // Nếu đang ở login/signup thì mới nhảy vào main
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == "login" || currentRoute == null) {
                    navController.navigate("main_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    // Xác định màn hình khởi đầu (chỉ dùng cho lần đầu load app)
    val startDestination = remember {
        if (client.auth.currentSessionOrNull() != null) "main_screen" else "login"
    }

    AppNavigation(
        startDestination = startDestination,
        navController = navController
    )
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    EndangeredAnimalsTheme {
        App()
    }
}
