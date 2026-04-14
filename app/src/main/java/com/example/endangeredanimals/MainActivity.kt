package com.example.endangeredanimals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.Navigation.AppNavigation
import com.example.endangeredanimals.Network.SupabaseInstance
import com.example.endangeredanimals.ui.AppBottomNavBackground
import com.example.endangeredanimals.ui.AppPrimaryColor
import com.example.endangeredanimals.ui.EndangeredAnimalsTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

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
    val navController = rememberNavController()
    val client = SupabaseInstance.client
    
    val sessionStatus by client.auth.sessionStatus.collectAsState()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionStatus) {
        startDestination = when (sessionStatus) {
            is SessionStatus.Authenticated -> "home"
            is SessionStatus.NotAuthenticated -> "login"
            else -> null
        }
    }

    if (startDestination != null) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val fullScreenRoutes = listOf(
            "login",
            "signup_screen",
            "forgotpassword_screen",
            "result_screen",
            "animal_screen/{animalId}",
            "changepassword_screen",
            "story_selection",
            "story_play/{gameId}"
        )
        // Kiểm tra logic hiển thị bar chặt chẽ hơn
        val shouldShowBars = currentRoute != null && currentRoute !in fullScreenRoutes

        Scaffold(
            topBar = {
                if (shouldShowBars) {
                    MainTopAppBar(
                        onSearchNavigate = {
                            if (currentRoute != "result_screen") {
                                navController.navigate("result_screen")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (shouldShowBars) {
                    MainBottomBar(navController = navController)
                }
            }
        ) { innerPadding ->
            AppNavigation(
                startDestination = startDestination!!,
                navController = navController,
                modifier = if (shouldShowBars) Modifier.padding(innerPadding) else Modifier
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopAppBar(onSearchNavigate: () -> Unit) {
    TopAppBar(
        colors = topAppBarColors(containerColor = AppPrimaryColor),
        title = {
            Image(
                painter = painterResource(id = R.drawable.protect_animals),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(25.dp))
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Button(
                    onClick = onSearchNavigate,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Tìm kiếm",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Thông báo",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    )
}

@Composable
private fun MainBottomBar(navController: NavController) {
    val muc = listOf(
        Triple("Home", "home", R.drawable.home),
        Triple("Ask_AI", "ask_ai", R.drawable.icon_chat),
        Triple("Favorite", "favorite_screen", R.drawable.favorite),
        Triple("Profile", "profile", R.drawable.profile)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        containerColor = AppBottomNavBackground,
    ) {
        muc.forEach { (name, route, iconRes) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Quay về start destination của graph để tránh tích tụ stack
                            popUpTo(navController.graph.findStartDestination().id) { 
                                saveState = true 
                            }
                            // Tránh tạo nhiều bản sao của cùng một đích đến
                            launchSingleTop = true
                            // Khôi phục trạng thái khi chọn lại tab cũ
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        tint = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
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
