package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    private val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"
    private val client = SupabaseInstance.client

    // Lưu từ khóa tìm kiếm để không bị mất khi quay lại màn hình
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
                
                val results = animals.filter { 
                    it.nameVn?.lowercase()?.contains(normalizedQuery) == true ||
                    it.nameLatin?.lowercase()?.contains(normalizedQuery) == true ||
                    it.animalGroup?.lowercase()?.contains(normalizedQuery) == true
                }.map { animal ->
                    if (!animal.imageUrl.isNullOrBlank() && !animal.imageUrl!!.startsWith("http")) {
                        animal.copy(imageUrl = STORAGE_BASE_URL + animal.imageUrl)
                    } else {
                        animal
                    }
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
