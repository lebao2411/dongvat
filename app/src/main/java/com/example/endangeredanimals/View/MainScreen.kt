package com.example.endangeredanimals.View

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ui.BottomNavBackground
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            MainTopAppBar(
                onSearchNavigate = {
                    // Khi tìm kiếm, ta muốn nhảy ra ngoài hẳn Scaffold nên dùng rootNavController
                    rootNavController.navigate("result_screen")
                }
            )
        },
        bottomBar = {
            MainBottomBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        // NAVHOST NỘI BỘ: Đã thêm hiệu ứng Fade In/Out cực mượt cho 4 Tab
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("home") { HomeScreen(navController = rootNavController) }
            composable("scan") { ScannerScreen(navController = rootNavController) }
            composable("favorite") { FavoriteScreen(navController = rootNavController) }
            composable("menu") {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                MenuScreen(
                    onNavigateToProfile = {
                        rootNavController.navigate("profile")
                    },
                    onNavigateToContribution = {
                        rootNavController.navigate("contribution")
                    },
                    onNavigateToDiscuss = {
                        rootNavController.navigate("discuss")
                    },
                    onLogout = {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                // 1. Xóa trí nhớ của Google
                                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                ).build()
                                val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut()

                                // 2. Đăng xuất khỏi Supabase.
                                com.example.endangeredanimals.Network.SupabaseInstance.client.auth.signOut()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopAppBar(onSearchNavigate: () -> Unit) {
    TopAppBar(
        colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
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
        Triple("Scan", "scan", R.drawable.scanner),
        Triple("Favorite", "favorite", R.drawable.favorite),
        Triple("Menu", "menu", R.drawable.menu)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        color = BottomNavBackground
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            muc.forEach { (name, route, iconRes) ->
                val isSelected = currentRoute == route

                val iconSize by animateDpAsState(
                    targetValue = if (isSelected) 30.dp else 24.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "size_animation_$name"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        tint = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}