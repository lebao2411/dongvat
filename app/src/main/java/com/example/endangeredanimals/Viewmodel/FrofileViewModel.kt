package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Account
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    var accountState by mutableStateOf<Account?>(null)
        private set

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    var showDeleteConfirmation by mutableStateOf(false)
        private set

    init {
        // Đổi tên hàm để rõ ràng hơn
        fetchOrCreateUserData()
    }

    fun fetchOrCreateUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _profileState.value = ProfileState.Loading
            }

            val user = auth.currentUser
            if (user == null) {
                withContext(Dispatchers.Main) {
                    _profileState.value = ProfileState.Error("Không tìm thấy người dùng. Vui lòng đăng nhập lại.")
                }
                return@launch
            }

            try {
                // Document ID chính là UID của người dùng
                val documentId = user.uid
                val docRef = db.collection("accounts").document(documentId)
                val documentSnapshot = docRef.get().await()

                if (documentSnapshot.exists()) {
                    Log.d("ProfileViewModel", "Document người dùng đã tồn tại. Đang đọc...")
                    val account = documentSnapshot.toObject<Account>()
                    withContext(Dispatchers.Main) {
                        accountState = account
                        _profileState.value = ProfileState.Idle
                    }
                } else {
                    Log.d("ProfileViewModel", "Document người dùng chưa tồn tại. Đang tạo mới...")
                    val newAccount = Account(
                        userName = user.displayName ?: "Người dùng mới", // Lấy tên từ Google hoặc đặt tên mặc định
                        email = user.email ?: ""
                    )
                    // Ghi document mới lên Firestore
                    docRef.set(newAccount).await()
                    Log.d("ProfileViewModel", "Tạo document mới thành công!")
                    withContext(Dispatchers.Main) {
                        accountState = newAccount
                        _profileState.value = ProfileState.Idle
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ProfileViewModel", "Lỗi khi tải hoặc tạo dữ liệu người dùng: ", e)
                    _profileState.value = ProfileState.Error("Lỗi khi xử lý dữ liệu: ${e.message}")
                }
            }
        }
    }

    fun updateUserInfo(newUserName: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _profileState.value = ProfileState.Error("Không thể cập nhật do thiếu thông tin người dùng.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _profileState.value = ProfileState.Loading }

            try {
                // Cập nhật trực tiếp bằng userId
                db.collection("accounts").document(userId).update("userName", newUserName).await()
                withContext(Dispatchers.Main) {
                    accountState = accountState?.copy(userName = newUserName)
                    _profileState.value = ProfileState.Success("Cập nhật thông tin thành công!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _profileState.value = ProfileState.Error("Cập nhật thất bại: ${e.message}")
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAccount(googleSignInClient: GoogleSignInClient) {
        val userToDelete = auth.currentUser
        if (userToDelete == null) {
            _profileState.value = ProfileState.Error("Không tìm thấy thông tin để xóa.")
            return
        }
        // Lấy ID trực tiếp từ user, không cần biến phụ
        val docIdToDelete = userToDelete.uid

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("accounts").document(docIdToDelete).delete().await()
                Log.d("ProfileViewModel", "Bước 1/3: Xóa document Firestore thành công.")

                userToDelete.delete().await()
                Log.d("ProfileViewModel", "Bước 2/3: Xóa người dùng khỏi Auth thành công.")

                googleSignInClient.revokeAccess().await()
                googleSignInClient.signOut().await()
                Log.d("ProfileViewModel", "Bước 3/3: Thu hồi quyền và đăng xuất Google thành công.")

                withContext(Dispatchers.Main) { auth.signOut() }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Lỗi nghiêm trọng trong quá trình xóa tài khoản: ", e)
                withContext(Dispatchers.Main) {
                    _profileState.value = ProfileState.Error("Lỗi khi xóa tài khoản. Vui lòng đăng nhập lại và thử lại. Lỗi: ${e.message}")
                }
            }
        }
    }

    fun onOpenDeleteDialog() {
        showDeleteConfirmation = true
    }

    fun onCloseDeleteDialog() {
        showDeleteConfirmation = false
    }

    fun clearState() {
        _profileState.value = ProfileState.Idle
    }
}
