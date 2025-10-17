package com.example.endangeredanimals.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Lớp đại diện cho các trạng thái có thể có của giao diện màn hình đăng ký.
 */
sealed class SignUpUIState {
    object Idle : SignUpUIState()      // Trạng thái ban đầu
    object Loading : SignUpUIState()   // Trạng thái đang xử lý đăng ký
    object Success : SignUpUIState()   // Trạng thái đăng ký thành công
    data class Error(val message: String) : SignUpUIState() // Trạng thái có lỗi
}

/**
 * ViewModel cho màn hình Đăng ký.
 */
class SignUpViewModel : ViewModel() {

    // --- State cho các ô nhập liệu ---
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    // --- State cho trạng thái của giao diện (UI) ---
    private val _signUpUIState = MutableStateFlow<SignUpUIState>(SignUpUIState.Idle)
    val signUpUIState = _signUpUIState.asStateFlow()

    // Lấy instance của Firebase Auth và Firestore
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- Hàm cập nhật giá trị ---
    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút Đăng ký.
     */
    fun onSignUpClick() {
        // --- 1. Validate đầu vào ---
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
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

        // --- 2. Thực hiện đăng ký với Firebase ---
        viewModelScope.launch {
            _signUpUIState.value = SignUpUIState.Loading
            try {
                // Tạo người dùng với email và mật khẩu
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                // Nếu tạo người dùng thành công, authResult.user sẽ không null
                authResult.user?.let { firebaseUser ->
                    // Tạo một đối tượng user để lưu vào Firestore
                    val user = hashMapOf(
                        "uid" to firebaseUser.uid,
                        "email" to firebaseUser.email,
                        "createdAt" to System.currentTimeMillis()
                        // Bạn có thể thêm các trường khác như displayName, photoUrl,... sau
                    )

                    // Lưu đối tượng user vào collection "users" với document ID là UID của người dùng
                    db.collection("users").document(firebaseUser.uid).set(user).await()

                    // Cập nhật trạng thái thành Công
                    _signUpUIState.value = SignUpUIState.Success
                } ?: run {
                    // Trường hợp hiếm gặp khi user là null dù không có exception
                    _signUpUIState.value = SignUpUIState.Error("Không thể lấy thông tin người dùng sau khi tạo.")
                }

            } catch (e: Exception) {
                // Bắt các lỗi từ Firebase (ví dụ: email đã tồn tại, lỗi mạng,...)
                val errorMessage = e.message ?: "Đã xảy ra lỗi không xác định."
                _signUpUIState.value = SignUpUIState.Error(errorMessage)
            }
        }
    }

    /**
     * Đặt lại trạng thái lỗi về Idle sau khi thông báo lỗi đã được hiển thị.
     */
    fun clearErrorState() {
        if (_signUpUIState.value is SignUpUIState.Error) {
            _signUpUIState.value = SignUpUIState.Idle
        }
    }
}
