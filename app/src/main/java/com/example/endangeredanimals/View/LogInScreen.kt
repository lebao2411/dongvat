package com.example.endangeredanimals.View

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.LoginUIState
import com.example.endangeredanimals.ViewModel.LoginViewModel
import com.example.endangeredanimals.ui.AppPrimaryColor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LogInScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    
    // Web Client ID lấy từ Google Cloud Console
    val webClientId = "752816137373-a09e3o6btcihvi22e9e7g0cmrc8rjl0p.apps.googleusercontent.com"

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loginViewModel.handleGoogleSignInResult(result.data)
        } else {
            Toast.makeText(context, "Đã hủy đăng nhập bằng Google", Toast.LENGTH_SHORT).show()
            loginViewModel.clearErrorState()
        }
    }

    val loginState by loginViewModel.loginUIState.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppPrimaryColor,
        unfocusedBorderColor = Color.Gray,
        cursorColor = AppPrimaryColor,
        focusedLabelColor = AppPrimaryColor,
    )

    // Xử lý các trạng thái từ ViewModel
    when (val state = loginState) {
        is LoginUIState.Success -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }

        is LoginUIState.Error -> {
            LaunchedEffect(state.message) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                loginViewModel.clearErrorState()
            }
        }
        else -> {}
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Đăng nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
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
                colors = textFieldColors
            )

            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Quên mật khẩu?")
                    }
                },
                onClick = { navController.navigate("forgotpassword_screen") },
                modifier = Modifier.fillMaxWidth(),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.End)
            )

            Button(
                onClick = {
                    loginViewModel.onLoginClick(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor),
                enabled = loginState !is LoginUIState.Loading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Đăng nhập", fontSize = 18.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "hoặc",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    loginViewModel.onGoogleSignInClick(launcher, googleSignInClient)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = loginState !is LoginUIState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng nhập với Google")
            }

            SignUpText(navController = navController)
        }

        if (loginState is LoginUIState.Loading) {
            CircularProgressIndicator(color = AppPrimaryColor)
        }
    }
}

@Composable
fun SignUpText(navController: NavController) {
    val annotatedString = buildAnnotatedString {
        append("Bạn chưa có tài khoản? ")
        pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("Đăng ký")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                .firstOrNull()?.let {
                    navController.navigate("signup_screen")
                }
        },
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun LogInScreenPreview() {
    LogInScreen(navController = rememberNavController())
}
