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

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    fun onSignUpClick(userName: String) { // Thêm userName vào đây
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
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                authResult.user?.let { firebaseUser ->

                    val accountData = hashMapOf(
                        "userName" to userName,
                        "email" to firebaseUser.email,
                        "habitatScore" to 0,
                        "conservationScore" to 0
                    )

                    //    Sử dụng UID của người dùng làm ID cho document
                    db.collection("accounts").document(firebaseUser.uid).set(accountData).await()

                    _signUpUIState.value = SignUpUIState.Success

                } ?: run {
                    _signUpUIState.value = SignUpUIState.Error("Không thể lấy thông tin người dùng sau khi tạo.")
                }

            } catch (e: Exception) {
                val errorMessage = when {
                    "email-already-in-use" in (e.message ?: "") -> "Email này đã được sử dụng."
                    else -> e.message ?: "Đã xảy ra lỗi không xác định."
                }
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
