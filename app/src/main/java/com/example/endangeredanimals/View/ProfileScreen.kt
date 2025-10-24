package com.example.endangeredanimals.View

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ProfileState
import com.example.endangeredanimals.ViewModel.ProfileViewModel
import com.example.endangeredanimals.ui.AppGrayBlue
import com.example.endangeredanimals.ui.AppPrimaryColor
import com.example.endangeredanimals.ui.AppWarningColor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val account = profileViewModel.accountState
    val profileState by profileViewModel.profileState.collectAsState()
    var userName by remember(account) { mutableStateOf(account?.userName ?: "") }
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
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

    LaunchedEffect(Firebase.auth.currentUser) {
        if (Firebase.auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (account == null || profileState is ProfileState.Loading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Thông tin cá nhân",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Tên người dùng") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppPrimaryColor,
                                focusedLabelColor = AppPrimaryColor,
                                cursorColor = AppPrimaryColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        InfoTextField(
                            value = account.email,
                            label = "Email",
                            icon = Icons.Default.Email
                        )
                        InfoTextField(
                            value = "${account.habitatScore} điểm",
                            label = "Điểm bảo vệ sinh cảnh",
                            iconPainter = painterResource(id = R.drawable.habitat)
                        )
                        InfoTextField(
                            value = "${account.conservationScore} điểm",
                            label = "Điểm bảo tồn động vật",
                            iconPainter = painterResource(id = R.drawable.animal)
                        )

                        Button(
                            onClick = { profileViewModel.updateUserInfo(userName) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                        ) {
                            Text("Lưu thay đổi")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        text = "Đổi mật khẩu",
                        onClick = { navController.navigate("changepassword_screen") },
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFb35715),
                        contentColor = Color.White
                    )

                    ActionButton(
                        text = "Xóa tài khoản",
                        onClick = { profileViewModel.onOpenDeleteDialog() },
                        modifier = Modifier.weight(1f),
                        containerColor = AppWarningColor,
                        contentColor = Color.White
                    )
                }

                Button(
                    onClick = {
                        profileViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGrayBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Đăng xuất",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Đăng xuất")
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

@Composable
fun InfoTextField(value: String, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {  },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun InfoTextField(value: String, label: String, iconPainter: Painter) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
