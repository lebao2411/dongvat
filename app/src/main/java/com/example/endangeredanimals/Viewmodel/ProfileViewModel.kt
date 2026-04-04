package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.Network.SupabaseInstance
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    var accountState by mutableStateOf<Account?>(null)
        private set

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var showDeleteConfirmation by mutableStateOf(false)
        private set

    init {
        fetchOrCreateUserData()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchUserDataFromSupabase()
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun fetchOrCreateUserData() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            fetchUserDataFromSupabase()
        }
    }

    private suspend fun fetchUserDataFromSupabase() {
        val user = client.auth.currentSessionOrNull()?.user
        if (user == null) {
            _profileState.value = ProfileState.Error("Vui lòng đăng nhập lại.")
            return
        }

        try {
            val result = client.from("accounts")
                .select {
                    filter {
                        eq("userId", user.id)
                    }
                }
                .decodeSingleOrNull<Account>()

            if (result != null) {
                accountState = result
                _profileState.value = ProfileState.Idle
            } else {
                // Tạo mới nếu chưa có
                val newAccount = Account(
                    userId = user.id,
                    userName = user.userMetadata?.get("full_name")?.toString() ?: "Người dùng mới",
                    email = user.email ?: ""
                )
                client.from("accounts").insert(newAccount)
                accountState = newAccount
                _profileState.value = ProfileState.Idle
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Supabase Error: ${e.message}")
            _profileState.value = ProfileState.Error("Lỗi tải dữ liệu: ${e.message}")
        }
    }

    fun updateUserInfo(newUserName: String) {
        val user = client.auth.currentSessionOrNull()?.user ?: return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                client.from("accounts").update(
                    {
                        set("userName", newUserName)
                    }
                ) {
                    filter {
                        eq("userId", user.id)
                    }
                }
                accountState = accountState?.copy(userName = newUserName)
                _profileState.value = ProfileState.Success("Cập nhật thành công!")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Cập nhật thất bại: ${e.message}")
            }
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient) {
        viewModelScope.launch {
            try {
                client.auth.signOut()
                googleSignInClient.signOut()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Sign out error", e)
            }
        }
    }

    fun deleteAccount(googleSignInClient: GoogleSignInClient) {
        val user = client.auth.currentSessionOrNull()?.user ?: return
        viewModelScope.launch {
            try {
                client.from("accounts").delete {
                    filter { eq("userId", user.id) }
                }
                client.auth.signOut()
                googleSignInClient.revokeAccess()
                googleSignInClient.signOut()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Delete error", e)
            }
        }
    }

    fun onOpenDeleteDialog() { showDeleteConfirmation = true }
    fun onCloseDeleteDialog() { showDeleteConfirmation = false }
    fun clearState() { _profileState.value = ProfileState.Idle }
}
