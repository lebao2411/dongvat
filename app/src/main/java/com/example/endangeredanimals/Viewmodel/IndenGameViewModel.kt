package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Network.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IndenQuizQuestion(
    val correctAnimal: Animal,
    val options: List<String>
)

sealed class IndenGameState {
    data object Loading : IndenGameState()
    data class Success(val question: IndenQuizQuestion) : IndenGameState()
    data object Error : IndenGameState()
}

class IndenGameViewModel : ViewModel() {

    private val client = SupabaseInstance.client
    private var allAnimalsForGame: List<Animal> = emptyList()

    private val _gameState = MutableStateFlow<IndenGameState>(IndenGameState.Loading)
    val gameState = _gameState.asStateFlow()

    fun loadGame() {
        if (allAnimalsForGame.isNotEmpty()) {
            generateNewQuestion()
            return
        }

        viewModelScope.launch {
            _gameState.value = IndenGameState.Loading
            try {
                // SUPABASE: Lấy danh sách động vật
                val animals = client.from("animals")
                    .select()
                    .decodeList<Animal>()

                val validAnimals = animals.filter { animal ->
                    !animal.imageUrl.isNullOrBlank() && !animal.nameVn.isNullOrBlank()
                }

                allAnimalsForGame = validAnimals

                if (allAnimalsForGame.size >= 4) {
                    generateNewQuestion()
                } else {
                    _gameState.value = IndenGameState.Error
                    Log.e("IndenGameViewModel", "Không đủ động vật hợp lệ (cần >= 4)")
                }

            } catch (e: Exception) {
                _gameState.value = IndenGameState.Error
                Log.e("IndenGameViewModel", "Supabase Error: ${e.message}")
            }
        }
    }

    fun generateNewQuestion() {
        if (allAnimalsForGame.size < 4) {
            _gameState.value = IndenGameState.Error
            return
        }

        val randomAnimals = allAnimalsForGame.shuffled().take(4)
        val correctAnswer = randomAnimals[0]
        val options = randomAnimals.mapNotNull { it.nameVn }.shuffled()
        
        val newQuestion = IndenQuizQuestion(
            correctAnimal = correctAnswer,
            options = options
        )

        _gameState.value = IndenGameState.Success(newQuestion)
    }
}
