package com.example.endangeredanimals.View

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.endangeredanimals.ui.Neutral100
import com.example.endangeredanimals.ui.Neutral50

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

    val state by forgotPasswordViewModel.forgotPasswordState.collectAsState()
    val context = LocalContext.current

    val isOtpVerified = state is ForgotPasswordState.OtpVerified || state is ForgotPasswordState.Success

    LaunchedEffect(state) {
        when (val currentState = state) {
            is ForgotPasswordState.OtpSent -> {
                // Đã cập nhật Toast theo ý muốn của bạn
                Toast.makeText(context, "Đã gửi mã thành công, mã có hiệu lực trong 1 giờ sau khi gửi", Toast.LENGTH_LONG).show()
                forgotPasswordViewModel.clearState()
            }
            is ForgotPasswordState.OtpVerified -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
            }
            is ForgotPasswordState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
                forgotPasswordViewModel.clearState()
            }
            is ForgotPasswordState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                forgotPasswordViewModel.clearState()
            }
            else -> { }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = Neutral50,
        topBar = {
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding() // TỰ ĐỘNG ĐẨY LÊN KHI BÀN PHÍM XUẤT HIỆN
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Neutral100)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Quên Mật Khẩu",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Nhập Email của bạn") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            readOnly = isOtpVerified
                        )

                        // NÚT GỬI MÃ NẰM CẠNH Ô NHẬP MÃ
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
                                shape = RoundedCornerShape(12.dp),
                                readOnly = isOtpVerified
                            )
                            Button(
                                onClick = { forgotPasswordViewModel.sendOtp(email) },
                                enabled = !isOtpVerified, // Nút luôn khả dụng nếu chưa xác thực xong
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Gửi mã")
                            }
                        }

                        // NÚT XÁC THỰC CHUYỂN XUỐNG DƯỚI
                        Button(
                            onClick = {
                                if (!isOtpVerified) forgotPasswordViewModel.verifyOtp(email, verificationCode)
                            },
                            enabled = !isOtpVerified,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (!isOtpVerified) "Xác Thực Mã OTP" else "Đã Xác Thực Thành Công")
                        }

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isOtpVerified,
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(if (isPasswordVisible) R.drawable.visibility else R.drawable.visibility_off),
                                        contentDescription = "Toggle password visibility",
                                        modifier = Modifier.size(20.dp)
                                    )
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
                            shape = RoundedCornerShape(12.dp),
                            enabled = isOtpVerified,
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(if (isConfirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off),
                                        contentDescription = "Toggle password visibility",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        Button(
                            onClick = { forgotPasswordViewModel.resetPassword(newPassword, confirmPassword) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = isOtpVerified,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Xác Nhận Đổi Mật Khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // LOADING NẰM TRÊN CÙNG
            if (state is ForgotPasswordState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
