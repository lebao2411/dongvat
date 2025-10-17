package com.example.endangeredanimals.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    object Success : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    private val _loginUIState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginUIState = _loginUIState.asStateFlow()

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onLoginClick() {
        if (email.isBlank() || password.isBlank()) {
            _loginUIState.value = LoginUIState.Error("Email và mật khẩu không được để trống.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginUIState.value = LoginUIState.Error("Định dạng email không hợp lệ.")
            return
        }

        viewModelScope.launch {
            _loginUIState.value = LoginUIState.Loading

            try {
                delay(2000)

                if (email == "test@example.com" && password == "123456") {
                    _loginUIState.value = LoginUIState.Success
                } else {
                    _loginUIState.value = LoginUIState.Error("Email hoặc mật khẩu không chính xác.")
                }
            } catch (e: Exception) {
                _loginUIState.value = LoginUIState.Error("Đã xảy ra lỗi: ${e.message}")
            }
        }
    }

    fun clearErrorState() {
        if (_loginUIState.value is LoginUIState.Error) {
            _loginUIState.value = LoginUIState.Idle
        }
    }
}
