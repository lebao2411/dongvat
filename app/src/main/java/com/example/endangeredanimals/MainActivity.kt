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
import com.example.endangeredanimals.ui.EndangeredAnimalsTheme
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import java.util.UUID

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

    // GIẢI PHÁP: TẠO CHÌA KHÓA ĐỘC NHẤT CHO TỪNG TRẠNG THÁI VÀ TÀI KHOẢN
    val appKey = remember(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                // Nếu đăng nhập, dùng ID của User làm chìa khóa
                val userId = (sessionStatus as SessionStatus.Authenticated).session.user?.id
                userId ?: UUID.randomUUID().toString() // Fallback an toàn
            }
            else -> {
                // Nếu đăng xuất, tạo một chìa khóa ngẫu nhiên mới toanh
                UUID.randomUUID().toString()
            }
        }
    }

    // Dùng appKey thay vì isAuthenticated (boolean)
    key(appKey) {
        val navController = rememberNavController() // Mỗi chìa khóa mới sẽ có một NavController mới hoàn toàn
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
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    EndangeredAnimalsTheme {
        App()
    }
}