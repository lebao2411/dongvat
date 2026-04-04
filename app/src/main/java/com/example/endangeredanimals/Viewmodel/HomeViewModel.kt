package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    // Đã cập nhật đúng link gốc và tên bucket 'animal_images' của bạn
    private val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"

    val suggestedCategories = listOf(
        "Cầy", "Gà", "Nhông", "Vịt", "Thạch Sùng",
        "Khỉ", "Voọc", "Vượn", "Chuột", "Dơi"
    )

    private val _animalItems = MutableStateFlow<List<Animal>>(emptyList())
    val animalItems = _animalItems.asStateFlow()

    private val _randomAnimalItems = MutableStateFlow<List<Animal>>(emptyList())
    val randomAnimalItems = _randomAnimalItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchAnimalsFromSupabase()
            updateRandomList()
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchAnimalsFromSupabase()
            updateRandomList()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchAnimalsFromSupabase() {
        try {
            val animals = SupabaseInstance.client
                .from("animals")
                .select()
                .decodeList<Animal>()

            // Nối tên file trong database với link gốc của Storage
            val processedAnimals = animals.map { animal ->
                if (!animal.imageUrl.isNullOrBlank() && !animal.imageUrl!!.startsWith("http")) {
                    animal.copy(imageUrl = STORAGE_BASE_URL + animal.imageUrl)
                } else {
                    animal
                }
            }

            _animalItems.value = processedAnimals
            Log.d("HomeViewModel", "Successfully fetched ${processedAnimals.size} animals from Supabase.")

        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching animals from Supabase", e)
        }
    }

    private suspend fun updateRandomList() {
        withContext(Dispatchers.Default) {
            val processedList = _animalItems.value
                .filter { !it.imageUrl.isNullOrBlank() && it.imageUrl != "Không rõ" }
                .shuffled()
                .take(20)
            _randomAnimalItems.value = processedList
        }
    }
}
