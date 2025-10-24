package com.example.endangeredanimals.ViewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.random.Random

// Lớp niêm phong để quản lý các trạng thái của màn hình
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState() // Trạng thái ban đầu
    object Loading : ForgotPasswordState() // Đang xử lý
    data class OtpSent(val message: String) : ForgotPasswordState() // Gửi mã thành công
    data class OtpVerified(val message: String) : ForgotPasswordState() // Xác thực mã thành công
    data class Success(val message: String) : ForgotPasswordState() // Đổi mật khẩu thành công
    data class Error(val message: String) : ForgotPasswordState() // Có lỗi xảy ra
}

class ForgotPasswordViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // StateFlow để giao diện (View) có thể lắng nghe sự thay đổi
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    // Lưu trữ email và mã OTP đã xác thực để sử dụng ở các bước sau
    private var verifiedEmail: String? = null
    private var verifiedOtp: String? = null

    /**
     * Hàm xử lý khi người dùng nhấn nút "Gửi mã"
     */
    fun sendOtp(email: String) {
        viewModelScope.launch {
            // 1. Kiểm tra định dạng email
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Địa chỉ email không hợp lệ.")
                return@launch
            }

            _forgotPasswordState.value = ForgotPasswordState.Loading

            try {
                // 2. Kiểm tra xem email có tồn tại trong hệ thống (collection 'accounts') không
                val accountsCollection = db.collection("accounts")
                val querySnapshot = accountsCollection.whereEqualTo("email", email).limit(1).get().await()

                if (querySnapshot.isEmpty) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Email không tồn tại trong hệ thống.")
                    return@launch
                }

                val userDocument = querySnapshot.documents.first()
                val userId = userDocument.id // Lấy ID của document trong 'accounts'

                // 3. Tạo mã OTP và thời gian hết hạn
                val otpCode = String.format("%06d", Random.nextInt(100000, 999999))
                val expiryTime = Date(System.currentTimeMillis() + 5 * 60 * 1000) // Hết hạn sau 5 phút

                // 4. Lưu thông tin OTP vào collection 'otpCode'
                val otpData = hashMapOf(
                    "userId" to userId,
                    "email" to email,
                    "otpCode" to otpCode,
                    "expiryTime" to expiryTime,
                    "isUsed" to false
                )

                db.collection("otpCode").add(otpData).await()

                println("Gửi mã OTP '$otpCode' đến email '$email'")

                _forgotPasswordState.value = ForgotPasswordState.OtpSent("Mã xác nhận đã được gửi đến email của bạn.")

            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Đã xảy ra lỗi: ${e.message}")
            }
        }
    }

    /**
     * Hàm xử lý khi người dùng nhấn nút "Xác nhận mã" (hoặc tương đương)
     */
    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            if (otp.isBlank() || otp.length != 6) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mã OTP phải có 6 chữ số.")
                return@launch
            }
            _forgotPasswordState.value = ForgotPasswordState.Loading

            try {
                // Tìm mã OTP trong collection 'otpCode'
                val otpQuery = db.collection("otpCode")
                    .whereEqualTo("email", email)
                    .whereEqualTo("otpCode", otp)
                    .limit(1)
                    .get()
                    .await()

                if (otpQuery.isEmpty) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Mã OTP không chính xác.")
                    return@launch
                }

                val otpDocument = otpQuery.documents.first()
                val expiryTime = otpDocument.getDate("expiryTime")
                val isUsed = otpDocument.getBoolean("isUsed") ?: false

                // Kiểm tra xem mã đã được sử dụng hoặc hết hạn chưa
                if (isUsed) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Mã OTP này đã được sử dụng.")
                    return@launch
                }
                if (expiryTime != null && expiryTime.before(Date())) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Mã OTP đã hết hạn.")
                    return@launch
                }

                otpDocument.reference.update("isUsed", true).await()

                verifiedEmail = email
                verifiedOtp = otp

                _forgotPasswordState.value = ForgotPasswordState.OtpVerified("Xác thực mã thành công. Vui lòng nhập mật khẩu mới.")

            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi xác thực OTP: ${e.message}")
            }
        }
    }

    /**
     * Hàm xử lý khi người dùng nhấn nút "Xác nhận" đổi mật khẩu
     */
    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Vui lòng nhập đầy đủ mật khẩu.")
                return@launch
            }
            if (newPassword.length < 6) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mật khẩu mới phải có ít nhất 6 ký tự.")
                return@launch
            }
            if (newPassword != confirmPassword) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Mật khẩu mới không khớp.")
                return@launch
            }
            // Kiểm tra xem đã có email được xác thực chưa
            if (verifiedEmail == null) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Vui lòng xác thực mã OTP trước.")
                return@launch
            }

            _forgotPasswordState.value = ForgotPasswordState.Loading

            try {
                val accountsQuery = db.collection("accounts")
                    .whereEqualTo("email", verifiedEmail)
                    .limit(1)
                    .get()
                    .await()

                if (accountsQuery.isEmpty) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi: Không tìm thấy tài khoản để cập nhật.")
                    return@launch
                }
                val authUid = accountsQuery.documents.first().getString("auth_uid")

                if (authUid == null) {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi: Tài khoản không liên kết với hệ thống xác thực.")
                    return@launch
                }


                println("Yêu cầu đổi mật khẩu cho auth_uid: $authUid với mật khẩu mới: $newPassword. (Cần Cloud Function)")

                _forgotPasswordState.value = ForgotPasswordState.Success("Yêu cầu đổi mật khẩu đã được gửi. Vui lòng kiểm tra lại sau.")

            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Lỗi khi đặt lại mật khẩu: ${e.message}")
            }
        }
    }

    /**
     * Hàm để xóa trạng thái, tránh hiển thị lại Toast khi xoay màn hình
     */
    fun clearState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}
