package com.example.endangeredanimals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.Navigation.AppNavigation
import com.example.endangeredanimals.Network.SupabaseInstance
import com.example.endangeredanimals.ui.BottomNavBackground
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

// MainActivity.kt
@Composable
fun App() {
    val navController = rememberNavController()
    val client = SupabaseInstance.client
    val sessionStatus by client.auth.sessionStatus.collectAsState()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionStatus) {
        startDestination = when (sessionStatus) {
            is SessionStatus.Authenticated -> "main_screen"
            is SessionStatus.NotAuthenticated -> "login"
            else -> null
        }
    }

    if (startDestination != null) {
        AppNavigation(
            startDestination = startDestination!!,
            navController = navController
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    EndangeredAnimalsTheme {
        App()
    }
}
