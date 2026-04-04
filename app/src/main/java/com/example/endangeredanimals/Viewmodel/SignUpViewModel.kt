package com.example.endangeredanimals.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignUpUIState {
    object Idle : SignUpUIState()
    object Loading : SignUpUIState()
    object Success : SignUpUIState()
    data class Error(val message: String) : SignUpUIState()
}

class SignUpViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    private val _signUpUIState = MutableStateFlow<SignUpUIState>(SignUpUIState.Idle)
    val signUpUIState = _signUpUIState.asStateFlow()

    private val client = SupabaseInstance.client

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    fun onSignUpClick(userName: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || userName.isBlank()) {
            _signUpUIState.value = SignUpUIState.Error("Vui lòng nhập đầy đủ thông tin.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signUpUIState.value = SignUpUIState.Error("Định dạng email không hợp lệ.")
            return
        }
        if (password.length < 6) {
            _signUpUIState.value = SignUpUIState.Error("Mật khẩu phải có ít nhất 6 ký tự.")
            return
        }
        if (password != confirmPassword) {
            _signUpUIState.value = SignUpUIState.Error("Mật khẩu xác nhận không khớp.")
            return
        }

        viewModelScope.launch {
            _signUpUIState.value = SignUpUIState.Loading
            try {
                // 1. SUPABASE: Tạo tài khoản Auth
                val user = client.auth.signUpWith(Email) {
                    this.email = this@SignUpViewModel.email
                    this.password = this@SignUpViewModel.password
                }

                if (user != null) {
                    // 2. SUPABASE: Lưu thông tin bổ sung vào bảng accounts
                    val account = Account(
                        userId = user.id,
                        userName = userName,
                        email = user.email ?: "",
                        habitatScore = 0,
                        conservationScore = 0
                    )
                    client.from("accounts").insert(account)
                    _signUpUIState.value = SignUpUIState.Success
                } else {
                    _signUpUIState.value = SignUpUIState.Error("Đăng ký thành công nhưng không lấy được thông tin người dùng.")
                }

            } catch (e: Exception) {
                val errorMessage = when {
                    "already registered" in (e.message ?: "") -> "Email này đã được sử dụng."
                    else -> e.message ?: "Đã xảy ra lỗi không xác định."
                }
                _signUpUIState.value = SignUpUIState.Error(errorMessage)
            }
        }
    }

    fun clearErrorState() {
        if (_signUpUIState.value is SignUpUIState.Error) {
            _signUpUIState.value = SignUpUIState.Idle
        }
    }
}
