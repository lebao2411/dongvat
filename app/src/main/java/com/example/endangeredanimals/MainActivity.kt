package com.example.endangeredanimals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.Navigation.AppNavigation
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.ViewModel.HomeViewModel
import com.example.endangeredanimals.ui.EndangeredAnimalsTheme
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    // Biến trạng thái để kiểm soát việc bật/tắt Splash Screen
    private var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khóa Splash Screen lại chừng nào biến này còn là true
        splashScreen.setKeepOnScreenCondition { keepSplashOpened }

        setContent {
            EndangeredAnimalsTheme {
                App(
                    homeViewModel = homeViewModel,
                    onAppReady = { keepSplashOpened = false } // Hàm callback để tắt Splash
                )
            }
        }
    }
}

@Composable
fun App(
    homeViewModel: HomeViewModel,
    onAppReady: () -> Unit
) {
    val client = SupabaseInstance.client
    val navController = rememberNavController()

    // Khởi tạo null để không vẽ giao diện vội
    var startDestination by remember { mutableStateOf<String?>(null) }
    val isHomeLoading by homeViewModel.isLoading.collectAsState()

    // 1. Phân tích quyền (Role) ngay khi mở App
    LaunchedEffect(Unit) {
        // Đợi cho đến khi Supabase xác định được trạng thái session (tránh race condition khi load từ storage)
        client.auth.sessionStatus.first { it !is SessionStatus.LoadingFromStorage }
        android.util.Log.d("AppLaunch", "Session status determined: ${client.auth.sessionStatus.value}")

        val session = client.auth.currentSessionOrNull()
        if (session == null) {
            android.util.Log.d("AppLaunch", "No session found, navigating to login")
            startDestination = "login"
        } else {
            try {
                // Chọc thẳng vào bảng accounts để lấy role
                val account = client.from("accounts")
                    .select { filter { eq("userId", session.user!!.id) } }
                    .decodeSingleOrNull<Account>()

                val role = account?.role ?: "user"
                android.util.Log.d("AppLaunch", "Session found for user: ${session.user!!.id}, role: $role")
                // Quyết định điểm đến chuẩn xác 100%
                startDestination = if (role == "admin") "admin_management" else "main_screen"
            } catch (e: Exception) {
                android.util.Log.e("AppLaunch", "Error fetching role", e)
                startDestination = "login"
            }
        }
    }

    // 2. Quyết định thời điểm tắt Splash Screen một cách thông minh
    LaunchedEffect(startDestination, isHomeLoading) {
        if (startDestination == "login" || startDestination == "admin_management") {
            // Nếu là Admin hoặc chưa đăng nhập -> Không cần chờ tải động vật, TẮT SPLASH NGAY
            onAppReady()
        } else if (startDestination == "main_screen" && !isHomeLoading) {
            // Nếu là User thường -> Đợi tải danh sách động vật xong rồi mới TẮT SPLASH
            onAppReady()
        }
    }

    // 3. Lắng nghe trạng thái đăng xuất (Chỉ xử lý khi người dùng thực sự bấm Đăng xuất)
    val sessionStatus by client.auth.sessionStatus.collectAsState()
    LaunchedEffect(sessionStatus) {
        // CHỈ navigate khi session chuyển sang NotAuthenticated SAU KHI app đã load xong (startDestination != null)
        // VÀ điểm đến hiện tại KHÔNG PHẢI là login (để tránh gọi navigate thừa khi mới mở app)
        if (sessionStatus is SessionStatus.NotAuthenticated && startDestination != null && startDestination != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // 4. Chỉ dựng NavHost (Hiển thị giao diện) khi đã xác định được điểm đến
    if (startDestination != null) {
        AppNavigation(
            startDestination = startDestination!!,
            navController = navController
        )
    }
}
