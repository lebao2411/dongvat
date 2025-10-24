package com.example.endangeredanimals.ViewModel

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    object Success : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}

class LoginViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

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
                auth.signInWithEmailAndPassword(email, password).await()
                _loginUIState.value = LoginUIState.Success
            } catch (e: Exception) {
                _loginUIState.value = LoginUIState.Error("Email hoặc mật khẩu không chính xác.")
            }
        }
    }

    // --- ĐĂNG NHẬP BẰNG GOOGLE ---

    fun onGoogleSignInClick(
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        googleSignInClient: GoogleSignInClient
    ) {
        // Luôn hiển thị trạng thái loading ngay khi bấm nút
        _loginUIState.value = LoginUIState.Loading
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            _loginUIState.value = LoginUIState.Error("Đăng nhập Google thất bại: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user!!

                val userDocRef = db.collection("accounts").document(user.uid)
                val document = userDocRef.get().await()

                if (!document.exists()) {
                    saveUserToFirestore(
                        userId = user.uid,
                        userName = user.displayName ?: "Người dùng mới",
                        email = user.email!!
                    )
                }

                _loginUIState.value = LoginUIState.Success
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Firebase Auth Error: ${e.message}")
                android.util.Log.e("LoginViewModel", "Error Type: ${e.javaClass.simpleName}")
                android.util.Log.e("LoginViewModel", "ID Token: ${idToken.take(20)}...")

                _loginUIState.value = LoginUIState.Error("Xác thực Google với Firebase thất bại.")
            }
        }
    }

    private fun saveUserToFirestore(userId: String, userName: String, email: String) {
        val userMap = hashMapOf(
            "userName" to userName,
            "email" to email,
            "password" to "",
            "habitatScore" to 0,
            "conservationScore" to 0
        )

        db.collection("accounts").document(userId)
            .set(userMap)
            .addOnFailureListener { e ->
                println("Lỗi khi lưu người dùng vào Firestore: ${e.message}")
            }
    }


    fun clearErrorState() {
        _loginUIState.value = LoginUIState.Idle
    }
}
