package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.Contribution
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscussViewModel : ViewModel() {

    // 1. STATE CHỨA DANH SÁCH BÀI ĐĂNG THẢO LUẬN
    private val _contributions = MutableStateFlow<List<Contribution>>(emptyList())
    val contributions: StateFlow<List<Contribution>> = _contributions.asStateFlow()

    // 2. STATE ĐỂ HIỆN VÒNG XOAY LOADING
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 3. STATE BÁO LỖI (NẾU CÓ)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Vừa mở màn hình lên là tự động gọi hàm lấy dữ liệu luôn
        fetchDiscussingContributions()
    }

    // HÀM KÉO DỮ LIỆU TỪ SUPABASE
    fun fetchDiscussingContributions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Gọi lên bảng "contributions"
                val result = SupabaseInstance.client.from("contributions")
                    .select {
                        // BÍ QUYẾT Ở ĐÂY: Chỉ lọc lấy những bài đang "discussing"
                        filter {
                            eq("status", "discussing")
                        }
                    }
                    .decodeList<Contribution>() // Tự động convert JSON thành List<Contribution>

                withContext(Dispatchers.Main) {
                    _contributions.value = result // Đổ dữ liệu vào State
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Lỗi tải dữ liệu: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false // Tắt vòng xoay loading
                }
            }
        }
    }
}