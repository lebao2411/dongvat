package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Model.Favorite
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"

    private val client = SupabaseInstance.client

    private val _favoriteAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val favoriteAnimals = _favoriteAnimals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadFavoriteAnimals()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchFavoritesFromSupabase()
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun loadFavoriteAnimals() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchFavoritesFromSupabase()
            _isLoading.value = false
        }
    }

    private suspend fun fetchFavoritesFromSupabase() {
        val user = client.auth.currentSessionOrNull()?.user
        if (user == null) {
            _favoriteAnimals.value = emptyList()
            return
        }

        try {
            val favorites = client.from("favorites")
                .select {
                    filter {
                        eq("userId", user.id)
                    }
                }
                .decodeList<Favorite>()

            val animalIds = favorites.map { it.animalId }

            if (animalIds.isNotEmpty()) {
                val animalsList = client.from("animals")
                    .select {
                        filter {
                            isIn("animalId", animalIds)
                        }
                    }
                    .decodeList<Animal>()
                
                // Xử lý imageUrl nếu chỉ chứa tên file
                val processedAnimals = animalsList.map { animal ->
                    if (!animal.imageUrl.isNullOrBlank() && !animal.imageUrl!!.startsWith("http")) {
                        animal.copy(imageUrl = STORAGE_BASE_URL + animal.imageUrl)
                    } else {
                        animal
                    }
                }
                
                _favoriteAnimals.value = processedAnimals
            } else {
                _favoriteAnimals.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e("FavoriteViewModel", "Supabase Error: ${e.message}")
            _favoriteAnimals.value = emptyList()
        }
    }

    fun toggleFavorite(animalId: String, isCurrentlyFavorite: Boolean, onComplete: () -> Unit) {
        val user = client.auth.currentSessionOrNull()?.user
        if (user == null) {
            onComplete()
            return
        }

        viewModelScope.launch {
            try {
                if (isCurrentlyFavorite) {
                    client.from("favorites").delete {
                        filter {
                            eq("userId", user.id)
                            eq("animalId", animalId)
                        }
                    }
                } else {
                    val newFavorite = Favorite(userId = user.id, animalId = animalId)
                    client.from("favorites").insert(newFavorite)
                }
                fetchFavoritesFromSupabase()
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Toggle error: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }
}
