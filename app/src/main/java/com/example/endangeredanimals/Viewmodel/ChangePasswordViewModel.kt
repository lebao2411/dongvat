package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
    
sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

class ChangePasswordViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    //xử lý thay đổi mật khẩu
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

            val user = auth.currentUser
            if (user == null || user.email == null) {
                _changePasswordState.value = ChangePasswordState.Error("Không tìm thấy người dùng. Vui lòng đăng nhập lại.")
                return@launch
            }

            try {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
                user.reauthenticate(credential).await()
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Mật khẩu cũ không chính xác.")
                return@launch
            }

            // cập nhật mật khẩu
            try {
                user.updatePassword(newPass).await()
                _changePasswordState.value = ChangePasswordState.Success("Đổi mật khẩu thành công!")
            } catch (e: Exception) {
                val errorMessage = "Đã xảy ra lỗi khi cập nhật mật khẩu: ${e.message}"
                _changePasswordState.value = ChangePasswordState.Error(errorMessage)
            }
        }
    }


    /**
     * Đặt lại trạng thái sau khi hiển thị thông báo
     */
    fun clearState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}
