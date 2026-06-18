package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseHelper
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Component.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    // ĐÃ XÓA STORAGE_BASE_URL
    private val client = SupabaseInstance.client

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Animal>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchMessage = MutableStateFlow("Nhập từ khóa để tìm kiếm động vật...")
    val searchMessage = _searchMessage.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchAnimals(query: String?) {
        if (query.isNullOrBlank()) {
            clearSearch()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val normalizedQuery = query.lowercase().trim()

                val animals = client.from("animals")
                    .select()
                    .decodeList<Animal>()

                // RÚT GỌN LOGIC MAP Ở ĐÂY
                val results = animals.filter {
                    it.nameVn?.lowercase()?.contains(normalizedQuery) == true ||
                            it.nameLatin?.lowercase()?.contains(normalizedQuery) == true ||
                            it.animalGroup?.lowercase()?.contains(normalizedQuery) == true
                }.map { animal ->
                    animal.copy(imageUrl = SupabaseHelper.getFullImageUrl(animal.imageUrl))
                }

                _searchResults.value = results

                if (results.isEmpty()) {
                    _searchMessage.value = "Không tìm thấy kết quả nào cho '$query'."
                } else {
                    _searchMessage.value = ""
                }

            } catch (e: Exception) {
                Log.e("ResultViewModel", "Supabase Error: ${e.message}")
                _searchMessage.value = "Đã xảy ra lỗi khi tìm kiếm: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _searchMessage.value = "Nhập từ khóa để tìm kiếm động vật..."
        _isLoading.value = false
    }
}