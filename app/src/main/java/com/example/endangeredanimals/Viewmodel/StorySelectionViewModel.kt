package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Model.Game
import com.example.endangeredanimals.Model.UserGameScore
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameWithStatus(
    val game: Game,
    val animal: Animal?,
    val isPlayed: Boolean
)

class StoryGameViewModel : ViewModel() {

    private val client = SupabaseInstance.client
    private val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"

    private val _gamesList = MutableStateFlow<List<GameWithStatus>>(emptyList())
    val gamesList = _gamesList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadStoryGames()
    }

    fun loadStoryGames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = client.auth.currentSessionOrNull()?.user?.id

                // 1. Lấy danh sách games loại 'story'
                val games = client.from("games")
                    .select { filter { eq("game_type", "story") } }
                    .decodeList<Game>()

                // 2. Lấy danh sách con vật để lấy ảnh nền
                val animals = client.from("animals").select().decodeList<Animal>()

                // 3. Lấy lịch sử chơi của user này
                val playedGameIds = if (userId != null) {
                    client.from("user_game_scores")
                        .select { filter { eq("userId", userId) } }
                        .decodeList<UserGameScore>()
                        .map { it.gameId }
                        .toSet()
                } else emptySet()

                // 4. Kết hợp dữ liệu
                val result = games.map { game ->
                    val animal = animals.find { it.animalID == game.animalId }
                    val processedAnimal = animal?.let {
                        if (!it.imageUrl.isNullOrBlank() && !it.imageUrl!!.startsWith("http")) {
                            it.copy(imageUrl = STORAGE_BASE_URL + it.imageUrl)
                        } else it
                    }
                    
                    GameWithStatus(
                        game = game,
                        animal = processedAnimal,
                        isPlayed = playedGameIds.contains(game.id)
                    )
                }

                _gamesList.value = result
            } catch (e: Exception) {
                Log.e("StoryGameVM", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
