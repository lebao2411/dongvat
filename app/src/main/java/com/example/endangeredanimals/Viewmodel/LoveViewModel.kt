package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoveViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _favoriteAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val favoriteAnimals = _favoriteAnimals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    init {
        fetchFavoriteAnimals()
    }

    fun fetchFavoriteAnimals() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _message.value = "Vui lòng đăng nhập để xem danh sách yêu thích."
                _isLoading.value = false
                return@launch
            }

            try {
                val favoriteAnimalIds = db.collection("favorites")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.getString("animalId") }

                if (favoriteAnimalIds.isEmpty()) {
                    _message.value = "Danh sách yêu thích của bạn đang trống."
                    _favoriteAnimals.value = emptyList()
                } else {
                    val animals = db.collection("animals")
                        .whereIn("id", favoriteAnimalIds)
                        .get()
                        .await()
                        .toObjects(Animal::class.java)

                    _favoriteAnimals.value = animals
                    _message.value = ""
                }

            } catch (e: Exception) {
                _message.value = "Lỗi khi tải danh sách: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
