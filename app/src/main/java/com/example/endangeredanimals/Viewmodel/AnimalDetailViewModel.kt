package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimalDetailViewModel : ViewModel() {

    private val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"

    private val _animal = MutableStateFlow<Animal?>(null)
    val animal: StateFlow<Animal?> = _animal.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun refresh(animalId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchAnimalDetailsFromSupabase(animalId)
            _isRefreshing.value = false
        }
    }

    fun loadAnimalDetails(animalId: String) {
        if (animalId.isBlank() || (_animal.value?.animalID == animalId && !_isRefreshing.value)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            fetchAnimalDetailsFromSupabase(animalId)
            _isLoading.value = false
        }
    }

    private suspend fun fetchAnimalDetailsFromSupabase(animalId: String) {
        try {
            val result = SupabaseInstance.client
                .from("animals")
                .select {
                    filter {
                        eq("animalId", animalId)
                    }
                }
                .decodeSingle<Animal>()

            // Xử lý imageUrl nếu chỉ chứa tên file
            val processedAnimal = if (!result.imageUrl.isNullOrBlank() && !result.imageUrl!!.startsWith("http")) {
                result.copy(imageUrl = STORAGE_BASE_URL + result.imageUrl)
            } else {
                result
            }

            _animal.value = processedAnimal
            Log.d("AnimalDetailVM", "Successfully fetched animal: ${processedAnimal.nameVn}")

        } catch (e: Exception) {
            Log.e("AnimalDetailVM", "Error fetching animal from Supabase", e)
            _animal.value = null
        }
    }
}
