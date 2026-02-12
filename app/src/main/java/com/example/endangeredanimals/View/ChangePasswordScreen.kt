package com.example.endangeredanimals.View

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ChangePasswordState
import com.example.endangeredanimals.ViewModel.ChangePasswordViewModel
import com.example.endangeredanimals.ui.AppBottomNavBackground
import com.example.endangeredanimals.ui.AppPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    changePasswordViewModel: ChangePasswordViewModel = viewModel()
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val changePasswordState by changePasswordViewModel.changePasswordState.collectAsState()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    LaunchedEffect(changePasswordState) {
        when (val state = changePasswordState) {
            is ChangePasswordState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
                changePasswordViewModel.clearState()
            }
            is ChangePasswordState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                changePasswordViewModel.clearState()
            }
            else -> {  }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(AppBottomNavBackground)
    ) {
        TopAppBar(
            title = {  },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            },
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thay đổi mật khẩu",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    PasswordTextField(
                        password = oldPassword,
                        onPasswordChange = { oldPassword = it },
                        label = "Mật khẩu cũ"
                    )

                    PasswordTextField(
                        password = newPassword,
                        onPasswordChange = { newPassword = it },
                        label = "Mật khẩu mới (ít nhất 6 ký tự)"
                    )

                    PasswordTextField(
                        password = confirmNewPassword,
                        onPasswordChange = { confirmNewPassword = it },
                        label = "Nhập lại mật khẩu mới"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            changePasswordViewModel.changePassword(oldPassword, newPassword, confirmNewPassword)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = changePasswordState !is ChangePasswordState.Loading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                    ) {
                        Text("XÁC NHẬN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (changePasswordState is ChangePasswordState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
private fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off
            val description = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(id = image),
                    contentDescription = description,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppPrimaryColor,
            cursorColor = AppPrimaryColor,
            focusedLabelColor = AppPrimaryColor,
        )
    )
}

@Preview(showBackground = true, name = "Change Password Screen")
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(
        navController = rememberNavController()
    )
}
