package com.example.endangeredanimals.ViewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class OtpSent(val message: String) : ForgotPasswordState()
    data class OtpVerified(val message: String) : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    fun sendOtp(email: String) {
        viewModelScope.launch {
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Địa chỉ email không hợp lệ.")
                return@launch
            }

            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                // resetPasswordForEmail là method của Auth plugin
                client.auth.resetPasswordForEmail(email)
                _forgotPasswordState.value = ForgotPasswordState.OtpSent("Mã xác nhận đã được gửi đến email của bạn.")
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi gửi mã: ${e.message}")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            if (otp.isBlank() || otp.length < 6) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Vui lòng nhập mã OTP hợp lệ.")
                return@launch
            }

            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                // Sử dụng verifyEmailOtp cho xác thực qua Email
                client.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = email,
                    token = otp
                )
                _forgotPasswordState.value = ForgotPasswordState.OtpVerified("Xác thực thành công.")
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mã xác nhận không chính xác hoặc đã hết hạn.")
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mật khẩu không khớp.")
                return@launch
            }
            if (newPassword.length < 6) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mật khẩu phải có ít nhất 6 ký tự.")
                return@launch
            }

            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                // updateUser là method của Auth plugin
                client.auth.updateUser {
                    password = newPassword
                }
                _forgotPasswordState.value = ForgotPasswordState.Success("Đổi mật khẩu thành công!")
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi đặt lại mật khẩu: ${e.message}")
            }
        }
    }

    fun clearState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}
