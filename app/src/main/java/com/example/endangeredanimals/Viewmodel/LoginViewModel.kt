package com.example.endangeredanimals.ViewModel

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.Network.SupabaseInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
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
                _loginUIState.value = LoginUIState.Success
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Supabase Auth Error", e)
                _loginUIState.value = LoginUIState.Error("Email hoặc mật khẩu không chính xác.")
            }
        }
    }

    // --- ĐĂNG NHẬP BẰNG GOOGLE ---

    fun onGoogleSignInClick(
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        googleSignInClient: GoogleSignInClient
    ) {
        _loginUIState.value = LoginUIState.Loading
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            supabaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            _loginUIState.value = LoginUIState.Error("Đăng nhập Google thất bại: ${e.message}")
        }
    }

    private fun supabaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                // Đăng nhập vào Supabase bằng ID Token nhận được từ Google
                client.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                
                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val result = client.from("accounts")
                        .select {
                            filter {
                                eq("userId", user.id)
                            }
                        }
                        .decodeSingleOrNull<Account>()

                    if (result == null) {
                        saveUserToSupabase(
                            userId = user.id,
                            userName = user.userMetadata?.get("full_name")?.toString() ?: "Người dùng mới",
                            email = user.email ?: ""
                        )
                    }
                }

                _loginUIState.value = LoginUIState.Success
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Supabase Auth Google Error: ${e.message}")
                _loginUIState.value = LoginUIState.Error("Xác thực Google với Supabase thất bại.")
            }
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient) {
        viewModelScope.launch {
            try {
                client.auth.signOut()
                googleSignInClient.signOut()
                _loginUIState.value = LoginUIState.Idle
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error signing out: ${e.message}")
            }
        }
    }

    private suspend fun saveUserToSupabase(userId: String, userName: String, email: String) {
        val newAccount = Account(
            userId = userId,
            userName = userName,
            email = email,
            password = "",
            habitatScore = 0,
            conservationScore = 0
        )

        try {
            client.from("accounts").insert(newAccount)
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Lỗi khi lưu người dùng vào Supabase: ${e.message}")
        }
    }

    fun clearErrorState() {
        _loginUIState.value = LoginUIState.Idle
    }
}
