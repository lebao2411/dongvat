package com.example.endangeredanimals.View

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.SignUpUIState
import com.example.endangeredanimals.ViewModel.SignUpViewModel
import com.example.endangeredanimals.ui.Neutral50

@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: SignUpViewModel = viewModel() // KẾT NỐI VỚI VIEWMODEL TẠI ĐÂY
) {
    var username by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.signUpUIState.collectAsState()
    val context = LocalContext.current

    // XỬ LÝ CÁC TRẠNG THÁI THÀNH CÔNG / LỖI / CHỜ
    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUIState.Success -> {
                Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                navController.popBackStack() // Quay lại màn hình Login
            }
            is SignUpUIState.Error -> {
                Toast.makeText(context, (uiState as SignUpUIState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral50)
            .statusBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Đăng ký",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên người dùng") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // DÙNG BIẾN EMAIL CỦA VIEWMODEL
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // DÙNG BIẾN PASSWORD CỦA VIEWMODEL
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )

            // BỔ SUNG TRƯỜNG XÁC NHẬN MẬT KHẨU CHO KHỚP VỚI VIEWMODEL
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Xác nhận mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password Icon") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(id = if (confirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off),
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )

            Button(
                onClick = { viewModel.onSignUpClick(username) }, // THỰC THI LOGIC
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = uiState !is SignUpUIState.Loading // Khóa nút khi đang load
            ) {
                Text(if (uiState is SignUpUIState.Loading) "Đang xử lý..." else "Đăng ký", fontSize = 18.sp)
            }
        }

        // HIỂN THỊ VÒNG XOAY LOADING CHỐNG CHẠM
        if (uiState is SignUpUIState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}