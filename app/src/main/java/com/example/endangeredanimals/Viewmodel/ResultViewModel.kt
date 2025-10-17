package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResultViewModel : ViewModel() {

    private val _results = MutableStateFlow<List<Animal>>(emptyList())
    val results = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    fun fetchAnimalsByCategory(category: String) {
        if (category.isBlank()) {
            _results.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Giả sử category tương ứng với trường "species" trong Firestore
                val querySnapshot = db.collection("animals")
                    .whereEqualTo("species", category)
                    .get()
                    .await()

                val animalsList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<Animal>()?.apply {
                        animalID = document.id
                    }
                }
                _results.value = animalsList
                Log.d("ResultViewModel", "Fetched ${animalsList.size} animals for category: $category")

            } catch (e: Exception) {
                Log.e("ResultViewModel", "Error fetching animals by category", e)
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}