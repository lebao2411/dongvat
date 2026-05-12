package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseHelper
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Model.Favorite
import com.example.endangeredanimals.Component.SupabaseInstance
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    private val _favoriteAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val favoriteAnimals = _favoriteAnimals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        val user = client.auth.currentSessionOrNull()?.user
        if (user != null) {
            loadFavoriteAnimals()
        } else {
            _isLoading.value = false
            _favoriteAnimals.value = emptyList()
        }
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
        withContext(Dispatchers.IO) {
            val user = client.auth.currentSessionOrNull()?.user
            if (user == null) {
                _favoriteAnimals.value = emptyList()
                return@withContext
            }

            try {
                val favorites = client.from("favorites")
                    .select { filter { eq("userId", user.id) } }
                    .decodeList<Favorite>()

                val animalIds = favorites.map { it.animalId }

                if (animalIds.isNotEmpty()) {
                    val animalsList = client.from("animals")
                        .select { filter { isIn("animalId", animalIds) } }
                        .decodeList<Animal>()

                    // ĐÃ RÚT GỌN LẠI
                    val processedAnimals = animalsList.map { animal ->
                        animal.copy(imageUrl = SupabaseHelper.getFullImageUrl(animal.imageUrl))
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
    }

    fun toggleFavorite(animalId: String, isCurrentlyFavorite: Boolean, onComplete: () -> Unit) {
        val user = client.auth.currentSessionOrNull()?.user // Sửa auth -> gotrue
        if (user == null) {
            onComplete()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
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
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}