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

class HomeViewModel : ViewModel() {

    val suggestedCategories = listOf(
        "Cầy", "Gà", "Nhông", "Vịt", "Thạch Sùng",
        "Khỉ", "Voọc", "Vượn", "Chuột", "Dơi"
    )

    private val _animalItems = MutableStateFlow<List<Animal>>(emptyList())
    val animalItems = _animalItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchAnimalsFromFirestore()
    }

    private fun fetchAnimalsFromFirestore() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val querySnapshot = db.collection("animals")
                    .get()
                    .await()

                val animalsList = querySnapshot.documents.mapNotNull { document ->
                    val animal = document.toObject<Animal>()
                    animal?.copy(animalID = document.id)
                }


                _animalItems.value = animalsList
                Log.d("HomeViewModel", "Successfully fetched ${animalsList.size} animals.")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching animals from Firestore", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
