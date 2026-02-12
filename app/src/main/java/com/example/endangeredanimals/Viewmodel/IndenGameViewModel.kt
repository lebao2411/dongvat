package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Model.Animal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class IndenQuizQuestion(
    val correctAnimal: Animal,
    val options: List<String>
)

// Các trạng thái có thể có của màn hình game
sealed class IndenGameState {
    data object Loading : IndenGameState() // Trạng thái đang tải dữ liệu
    data class Success(val question: IndenQuizQuestion) : IndenGameState() // Trạng thái tải thành công và có câu hỏi
    data object Error : IndenGameState() // Trạng thái có lỗi xảy ra
}

class IndenGameViewModel : ViewModel() {

    private val db = Firebase.firestore
    private var allAnimalsForGame: List<Animal> = emptyList() // Biến để lưu trữ toàn bộ danh sách động vật hợp lệ

    private val _gameState = MutableStateFlow<IndenGameState>(IndenGameState.Loading)
    val gameState = _gameState.asStateFlow()


    fun loadGame() {
        // Nếu danh sách đã được tải rồi thì không cần tải lại, chỉ tạo câu hỏi mới
        if (allAnimalsForGame.isNotEmpty()) {
            generateNewQuestion()
            return
        }

        viewModelScope.launch {
            _gameState.value = IndenGameState.Loading
            try {

                val documents = db.collection("animals")
                    .limit(30) // Thêm dòng này để giới hạn số lượng
                    .get().await()

                val allAnimalsFromDb = documents.toObjects<Animal>().mapIndexed { index, animal ->
                    animal.animalID = documents.documents[index].id
                    animal
                }

                val validAnimals = allAnimalsFromDb.filter { animal ->
                    !animal.imageUrl.isNullOrBlank() && !animal.nameVn.isNullOrBlank()
                }


                allAnimalsForGame = validAnimals

                if (allAnimalsForGame.size >= 4) {
                    generateNewQuestion()
                } else {
                    _gameState.value = IndenGameState.Error
                    Log.e("IndenGameViewModel", "Không đủ động vật hợp lệ (cần >= 4). Tìm thấy: ${allAnimalsForGame.size}")
                }

            } catch (e: Exception) {
                _gameState.value = IndenGameState.Error
                Log.e("IndenGameViewModel", "Lỗi nghiêm trọng khi tải danh sách động vật: ", e)
            }
        }
    }

    fun generateNewQuestion() {
        _gameState.value = IndenGameState.Loading

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
