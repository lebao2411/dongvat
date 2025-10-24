package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath // <-- Import quan trọng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoriteViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _favoriteAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val favoriteAnimals = _favoriteAnimals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadFavoriteAnimals()
    }

    fun loadFavoriteAnimals() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FavoriteViewModel", "User is not logged in.")
            _isLoading.value = false
            _favoriteAnimals.value = emptyList() // Đảm bảo rỗng khi chưa đăng nhập
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val favoriteDocs = db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val animalIds = favoriteDocs.documents.mapNotNull { it.getString("animalId") }

                if (animalIds.isNotEmpty()) {

                    val animalsList = mutableListOf<Animal>()
                    animalIds.forEach { id ->
                        try {
                            val document = db.collection("animals").document(id).get().await()
                            if (document.exists()) {
                                val animal = document.toObject<Animal>()
                                if (animal != null) {
                                    animal.animalID = document.id // Gán ID cho model
                                    animalsList.add(animal)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FavoriteViewModel", "Failed to fetch animal with id: $id", e)
                        }
                    }
                    _favoriteAnimals.value = animalsList

                } else {
                    // Nếu không có ID nào, trả về danh sách rỗng
                    _favoriteAnimals.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorite animals", e)
                _favoriteAnimals.value = emptyList()
            } finally {
                _isLoading.value = false // Kết thúc trạng thái tải
            }
        }
    }


    fun toggleFavorite(
        animalId: String,
        isCurrentlyFavorite: Boolean,
        onComplete: () -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete()
            return
        }

        viewModelScope.launch {
            try {
                if (isCurrentlyFavorite) {
                    val favoriteQuery = db.collection("favorites")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("animalId", animalId)
                        .limit(1)
                        .get()
                        .await()
                    if (!favoriteQuery.isEmpty) {
                        val docIdToDelete = favoriteQuery.documents.first().id
                        db.collection("favorites").document(docIdToDelete).delete().await()
                    }
                } else {
                    val existingQuery = db.collection("favorites")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("animalId", animalId)
                        .limit(1)
                        .get()
                        .await()
                    if (existingQuery.isEmpty) {
                        val newFavorite = hashMapOf("userId" to userId, "animalId" to animalId)
                        db.collection("favorites").add(newFavorite).await()
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Lỗi khi cập nhật trạng thái yêu thích", e)
            } finally {
                onComplete()
            }
        }
    }
}
