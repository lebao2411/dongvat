package com.example.endangeredanimals.View

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.endangeredanimals.ViewModel.ForgotPasswordState
import com.example.endangeredanimals.ViewModel.ForgotPasswordViewModel
import com.example.endangeredanimals.ui.AppPrimaryColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // State từ ViewModel (Sử dụng package viết thường)
    val state by forgotPasswordViewModel.forgotPasswordState.collectAsState()
    val context = LocalContext.current

    var secondsRemaining by remember { mutableStateOf(0) }
    val countdownMinutes = 5
    val isCountingDown = secondsRemaining > 0

    // Các trường nhập mật khẩu chỉ được bật khi đã xác thực OTP thành công
    val isOtpVerified = state is ForgotPasswordState.OtpVerified || state is ForgotPasswordState.Success

    // Xử lý logic khi state thay đổi
    LaunchedEffect(state) {
        when (val currentState = state) {
            is ForgotPasswordState.OtpSent -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_SHORT).show()
                secondsRemaining = countdownMinutes * 60 // Bắt đầu đếm ngược
                forgotPasswordViewModel.clearState()
            }
            is ForgotPasswordState.OtpVerified -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
            }
            is ForgotPasswordState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                navController.popBackStack() // Quay về màn hình đăng nhập
                forgotPasswordViewModel.clearState()
            }
            is ForgotPasswordState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                forgotPasswordViewModel.clearState()
            }
            else -> { /* Không làm gì */ }
        }
    }

    // Logic đếm ngược
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
    }

    // Cài đặt giao diện Edge-to-Edge
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            TopAppBar(
                title = { },
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Quên Mật Khẩu",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // --- Trường nhập Email ---
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Nhập Email của bạn") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            readOnly = isOtpVerified
                        )

                        // --- Trường nhập OTP và nút Gửi/Xác nhận mã ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                label = { Text("Mã xác nhận") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                readOnly = isOtpVerified
                            )
                            Button(
                                onClick = {
                                    if (!isOtpVerified) {
                                        forgotPasswordViewModel.verifyOtp(email, verificationCode)
                                    }
                                },
                                enabled = !isOtpVerified,
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                            ) {
                                Text(if (!isOtpVerified) "Xác thực" else "Đã OK")
                            }
                        }

                        // --- Nút Gửi mã riêng và đồng hồ đếm ngược ---
                        Button(
                            onClick = { forgotPasswordViewModel.sendOtp(email) },
                            enabled = !isCountingDown && !isOtpVerified,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isCountingDown) "Gửi lại sau..." else "Gửi mã")
                        }

                        if (isCountingDown) {
                            val minutes = secondsRemaining / 60
                            val seconds = secondsRemaining % 60
                            Text(
                                text = "Mã sẽ hết hạn trong %02d:%02d".format(minutes, seconds),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }


                        // --- Các trường nhập mật khẩu mới ---
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(16.dp),
                            enabled = isOtpVerified,
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(painterResource(if (isPasswordVisible) R.drawable.visibility else R.drawable.visibility_off), "Toggle password visibility")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Nhập lại mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(16.dp),
                            enabled = isOtpVerified,
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(painterResource(if (isConfirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off), "Toggle password visibility")
                                }
                            }
                        )

                        Button(
                            onClick = { forgotPasswordViewModel.resetPassword(newPassword, confirmPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = isOtpVerified,
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                        ) {
                            Text("Xác Nhận Đổi Mật Khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (state is ForgotPasswordState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}
