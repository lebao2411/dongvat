package com.example.endangeredanimals.ViewModel

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.example.endangeredanimals.Model.Account
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    data class Success(val role: String) : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}

class LoginViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    private val _loginUIState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginUIState = _loginUIState.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _loginUIState.value = LoginUIState.Error("Vui lòng nhập đầy đủ email và mật khẩu.")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _loginUIState.value = LoginUIState.Error("Định dạng email không hợp lệ.")
                return@launch
            }

            _loginUIState.value = LoginUIState.Loading
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val account = client.from("accounts")
                        .select { filter { eq("userId", user.id) } }
                        .decodeSingleOrNull<Account>()
                    
                    val role = account?.role ?: "user"
                    _loginUIState.value = LoginUIState.Success(role)
                } else {
                    _loginUIState.value = LoginUIState.Error("Không thể lấy thông tin phiên đăng nhập.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Supabase Auth Error: ${e.message}")

                // PHÂN LOẠI LỖI ĐỂ BÁO CHO CHÍNH XÁC
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("Email not confirmed", ignoreCase = true)) {
                    _loginUIState.value = LoginUIState.Error("Tài khoản chưa được xác nhận. Vui lòng kiểm tra Email của bạn!")
                } else if (errorMsg.contains("Invalid login credentials", ignoreCase = true)) {
                    _loginUIState.value = LoginUIState.Error("Email hoặc mật khẩu không chính xác.")
                } else {
                    _loginUIState.value = LoginUIState.Error("Đăng nhập thất bại. Vui lòng thử lại.")
                }
            }
        }
    }

    // --- ĐĂNG NHẬP BẰNG GOOGLE ---

    fun onGoogleSignInClick(
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        googleSignInClient: GoogleSignInClient
    ) {
        _loginUIState.value = LoginUIState.Loading

        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleSignIn", "Google Auth Success: Email = ${account.email}, IDToken Length = ${account.idToken?.length ?: 0}")
            supabaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google Auth Error: StatusCode = ${e.statusCode}, Message = ${e.message}")
            // Bổ sung phân tích mã lỗi cụ thể từ Google để dev dễ sửa
            val detailMsg = when (e.statusCode) {
                7 -> "Lỗi mạng (Network Error). Vui lòng kiểm tra wifi/mạng di động."
                10 -> "Lỗi cấu hình (DEVELOPER_ERROR): Điển hình do chưa thêm SHA-1 vào Google Cloud Console hoặc sai Web Client ID."
                12500 -> "Lỗi mã cấu hình (SIGN_IN_FAILED)."
                12501 -> "Người dùng chủ động hủy chọn tài khoản (USER_CANCELLED)."
                else -> "Mã lỗi hệ thống Google: ${e.statusCode}"
            }
            _loginUIState.value = LoginUIState.Error("Đăng nhập Google thất bại: $detailMsg")
        }
    }

    private fun supabaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                Log.d("GoogleSignIn", "Connecting token with Supabase...")
                // Đăng nhập vào Supabase bằng ID Token nhận được từ Google
                client.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }

                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    Log.d("GoogleSignIn", "Supabase Auth Success: User ID = ${user.id}")
                    val account = client.from("accounts")
                        .select { filter { eq("userId", user.id) } }
                        .decodeSingleOrNull<Account>()

                    val role = account?.role ?: "user"
                    Log.d("GoogleSignIn", "User Role Loaded: $role")
                    _loginUIState.value = LoginUIState.Success(role)
                } else {
                    Log.e("GoogleSignIn", "Supabase Auth Error: Session is null after sign in.")
                    _loginUIState.value = LoginUIState.Error("Lỗi xác thực người dùng.")
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Supabase Auth Google Error: ${e.message}", e)
                _loginUIState.value = LoginUIState.Error("Xác thực Google với Supabase thất bại.")
            }
        }
    }

    fun clearErrorState() {
        _loginUIState.value = LoginUIState.Idle
    }
}