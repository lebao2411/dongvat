package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResultViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _searchResults = MutableStateFlow<List<Animal>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchMessage = MutableStateFlow("Nhập từ khóa để tìm kiếm động vật...")
    val searchMessage = _searchMessage.asStateFlow()

    fun searchAnimals(query: String?) {
        if (query.isNullOrBlank()) {
            clearSearch()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val normalizedQuery = query.lowercase().trim()
                val documents = db.collection("animals").get().await()
                val allAnimals = documents.toObjects<Animal>().mapIndexed { index, animal ->
                    animal.animalID = documents.documents[index].id
                    animal
                }

                val filteredResults = allAnimals.filter {
                    it.nameVn?.lowercase()?.contains(normalizedQuery) == true
                }

                _searchResults.value = filteredResults

                if (filteredResults.isEmpty()) {
                    _searchMessage.value = "Không tìm thấy kết quả nào cho '$query'."
                }

            } catch (e: Exception) {
                _searchMessage.value = "Đã xảy ra lỗi khi tìm kiếm."
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
