package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Model.Contribution
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// THÊM DATA CLASS NÀY ĐỂ BỌC DỮ LIỆU UPDATE CHUẨN XÁC CHO SUPABASE
@Serializable
data class ContributionUpdate(
    val status: String,
    val finalAnimalId: String?
)

class AdminViewModel : ViewModel() {

    private val _contributions = MutableStateFlow<List<Contribution>>(emptyList())
    val contributions = _contributions.asStateFlow()

    private val _accountsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val accountsMap = _accountsMap.asStateFlow()

    private val _animals = MutableStateFlow<List<Animal>>(emptyList())
    val animals = _animals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading = _isActionLoading.asStateFlow()

    init {
        fetchAllData()
    }

    fun fetchAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                fetchContributions()
                fetchAnimals()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchContributions() {
        try {
            val result = SupabaseInstance.client.from("contributions")
                .select {
                    filter {
                        // Chỉ lấy những bài chưa chốt kết quả (Chờ duyệt hoặc Đang thảo luận)
                        isIn("status", listOf("pending", "discussing"))
                    }
                    order("createdAt", order = Order.DESCENDING)
                }
                .decodeList<Contribution>()
            _contributions.value = result

            // Sau khi có list contributions, đi lấy tên người dùng tương ứng
            val accountIds = result.mapNotNull { it.accountId }.distinct()
            if (accountIds.isNotEmpty()) {
                val accounts = SupabaseInstance.client.from("accounts")
                    .select {
                        filter {
                            isIn("userId", accountIds)
                        }
                    }
                    .decodeList<Account>()

                val nameMap = accounts.associate { it.userId to it.userName }
                _accountsMap.value = nameMap
            }
        } catch (e: Exception) {
            Log.e("AdminVM", "Error fetching contributions: ${e.message}")
        }
    }

    private suspend fun fetchAnimals() {
        try {
            val result = SupabaseInstance.client.from("animals")
                .select()
                .decodeList<Animal>()
            _animals.value = result
        } catch (e: Exception) {
            Log.e("AdminVM", "Error fetching animals: ${e.message}")
        }
    }

    fun updateContribution(
        contributionId: String,
        newStatus: String,
        finalAnimalId: String?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isActionLoading.value = true
            try {
                // Đóng gói dữ liệu vào data class đã định nghĩa ở trên
                val updateData = ContributionUpdate(
                    status = newStatus,
                    finalAnimalId = finalAnimalId
                )

                // Gửi request update chuẩn form của Supabase Kotlin
                SupabaseInstance.client.from("contributions").update(updateData) {
                    filter {
                        eq("contributionId", contributionId)
                    }
                }

                // Refresh list: Bài viết nào đã duyệt/từ chối sẽ tự động biến mất khỏi danh sách chờ
                fetchContributions()

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "Error updating contribution: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            } finally {
                _isActionLoading.value = false
            }
        }
    }
}