package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

class ChangePasswordViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    fun changePassword(oldPass: String, newPass: String, confirmNewPass: String) {
        viewModelScope.launch {
            if (oldPass.isBlank() || newPass.isBlank() || confirmNewPass.isBlank()) {
                _changePasswordState.value = ChangePasswordState.Error("Vui lòng điền đầy đủ thông tin.")
                return@launch
            }
            if (newPass != confirmNewPass) {
                _changePasswordState.value = ChangePasswordState.Error("Mật khẩu mới không khớp.")
                return@launch
            }
            if (newPass.length < 6) {
                _changePasswordState.value = ChangePasswordState.Error("Mật khẩu mới phải có ít nhất 6 ký tự.")
                return@launch
            }
            if (oldPass == newPass) {
                _changePasswordState.value = ChangePasswordState.Error("Mật khẩu mới không được trùng với mật khẩu cũ.")
                return@launch
            }

            _changePasswordState.value = ChangePasswordState.Loading

            val currentUser = client.auth.currentSessionOrNull()?.user
            val email = currentUser?.email

            if (currentUser == null || email == null) {
                _changePasswordState.value = ChangePasswordState.Error("Không tìm thấy người dùng. Vui lòng đăng nhập lại.")
                return@launch
            }

            try {
                // Bước 1: Xác thực mật khẩu cũ bằng cách thử đăng nhập lại
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = oldPass
                }
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Mật khẩu cũ không chính xác.")
                return@launch
            }

            // Bước 2: Cập nhật mật khẩu mới
            try {
                client.auth.updateUser {
                    password = newPass
                }
                _changePasswordState.value = ChangePasswordState.Success("Đổi mật khẩu thành công!")
            } catch (e: Exception) {
                val errorMessage = "Đã xảy ra lỗi khi cập nhật mật khẩu: ${e.message}"
                _changePasswordState.value = ChangePasswordState.Error(errorMessage)
            }
        }
    }

    fun clearState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}
