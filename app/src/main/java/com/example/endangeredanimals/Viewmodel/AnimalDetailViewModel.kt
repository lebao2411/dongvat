package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnimalDetailViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _animal = MutableStateFlow<Animal?>(null)
    val animal: StateFlow<Animal?> = _animal.asStateFlow()

    // Hàm tải chi tiết Animal từ Firestore
    fun loadAnimalDetails(animalId: String) {
        if (animalId.isBlank() || _animal.value?.animalID == animalId) {
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ANIMAL_DETAIL_VM", "Starting to load animal with ID: $animalId")
                val documentSnapshot = db.collection("animals").document(animalId).get().await()

                if (documentSnapshot.exists()) {
                    val animalData = documentSnapshot.toObject<Animal>()
                    if (animalData != null) {
                        // Gán ID của document vào trường animalID trong model
                        animalData.animalID = documentSnapshot.id
                        _animal.value = animalData
                        Log.d("ANIMAL_DETAIL_VM", "Successfully loaded animal: ${animalData.nameVn}")
                    } else {
                        Log.e("ANIMAL_DETAIL_VM", "Failed to convert document to Animal object for ID: $animalId")
                        _animal.value = null
                    }
                } else {
                    Log.e("ANIMAL_DETAIL_VM", "No document found for animal ID: $animalId")
                    _animal.value = null
                }
            } catch (e: Exception) {
                Log.e("ANIMAL_DETAIL_VM", "Error loading animal details for ID: $animalId", e)
                _animal.value = null
            }
        }
    }
}
