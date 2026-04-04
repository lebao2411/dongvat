@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ProfileState
import com.example.endangeredanimals.ViewModel.ProfileViewModel
import com.example.endangeredanimals.ui.AppBackgroundCard
import com.example.endangeredanimals.ui.AppButtonChangePW
import com.example.endangeredanimals.ui.AppGrayBlue
import com.example.endangeredanimals.ui.AppPrimaryColor
import com.example.endangeredanimals.ui.AppWarningColor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val account = profileViewModel.accountState
    val profileState by profileViewModel.profileState.collectAsState()
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()
    var userName by remember(account) { mutableStateOf(account?.userName ?: "") }
    
    val pullToRefreshState = rememberPullToRefreshState()

    val webClientId = "752816137373-a09e3o6btcihvi22e9e7g0cmrc8rjl0p.apps.googleusercontent.com"

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearState()
            }
            is ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                profileViewModel.clearState()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        if (SupabaseInstance.client.auth.currentSessionOrNull() == null) {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { profileViewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = Color.White,
                    color = Color(0xFF37ab3c),
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (account == null || profileState is ProfileState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF37ab3c))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = AppBackgroundCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(100.dp),
                                tint = AppPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = account.email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Tên người dùng") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { profileViewModel.updateUserInfo(userName) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                            ) {
                                Text("Lưu thay đổi")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ScoreCard(
                            modifier = Modifier.weight(1f),
                            label = "Bảo vệ sinh cảnh",
                            score = account.habitatScore ?: 0,
                            icon = painterResource(id = R.drawable.habitat)
                        )
                        ScoreCard(
                            modifier = Modifier.weight(1f),
                            label = "Bảo tồn động vật",
                            score = account.conservationScore ?: 0,
                            icon = painterResource(id = R.drawable.habitat) 
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("changepassword_screen") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppButtonChangePW,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Đổi mật khẩu")
                        }

                        Button(
                            onClick = {
                                profileViewModel.signOut(googleSignInClient)
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppGrayBlue)
                        ) {
                            Text("Đăng xuất")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { profileViewModel.onOpenDeleteDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppWarningColor),
                        contentPadding = PaddingValues(horizontal = 32.dp)
                    ) {
                        Text("Xóa tài khoản")
                    }
                }
            }

            if (profileViewModel.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { profileViewModel.onCloseDeleteDialog() },
                    title = { Text("Xác nhận xóa tài khoản") },
                    text = { Text("Bạn có chắc chắn muốn xóa tài khoản vĩnh viễn không? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                profileViewModel.onCloseDeleteDialog()
                                profileViewModel.deleteAccount(googleSignInClient)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppWarningColor)
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { profileViewModel.onCloseDeleteDialog() }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ScoreCard(
    modifier: Modifier = Modifier,
    label: String,
    score: Int,
    icon: Painter
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = AppPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$score điểm",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
