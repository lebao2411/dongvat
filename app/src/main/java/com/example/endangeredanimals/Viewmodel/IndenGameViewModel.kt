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

// Lớp dữ liệu để chứa một câu hỏi hoàn chỉnh của game
data class IndenQuizQuestion(
    val correctAnimal: Animal,
    val options: List<String> // Danh sách các tên Tiếng Việt để làm đáp án
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

    // StateFlow để giao diện (UI) có thể lắng nghe và tự động cập nhật
    private val _gameState = MutableStateFlow<IndenGameState>(IndenGameState.Loading)
    val gameState = _gameState.asStateFlow()

    init {
        // Bắt đầu tải danh sách động vật ngay khi ViewModel được tạo
        fetchAllAnimals()
    }

    // Hàm này chỉ chạy một lần để lấy toàn bộ dữ liệu cần thiết cho game
    private fun fetchAllAnimals() {
        viewModelScope.launch {
            try {
                // 1. Tải 30 document ĐẦU TIÊN từ collection "animals".
                val documents = db.collection("animals")
                    .limit(30) // Thêm dòng này để giới hạn số lượng
                    .get().await()

                val allAnimalsFromDb = documents.toObjects<Animal>().mapIndexed { index, animal ->
                    animal.animalID = documents.documents[index].id
                    animal
                }

                // 2. Lọc dữ liệu ở phía client (trong ứng dụng)
                val validAnimals = allAnimalsFromDb.filter { animal ->
                    !animal.imageUrl.isNullOrBlank() && !animal.nameVn.isNullOrBlank()
                }


                allAnimalsForGame = validAnimals

                // 3. Kiểm tra xem có đủ dữ liệu để chơi không (cần ít nhất 4 con vật)
                if (allAnimalsForGame.size >= 4) {
                    // Nếu đủ, tạo câu hỏi đầu tiên
                    generateNewQuestion()
                } else {
                    // Nếu không đủ, chuyển sang trạng thái lỗi
                    _gameState.value = IndenGameState.Error
                    Log.e("IndenGameViewModel", "Không đủ động vật hợp lệ (cần >= 4). Tìm thấy: ${allAnimalsForGame.size}")
                }

            } catch (e: Exception) {
                _gameState.value = IndenGameState.Error
                Log.e("IndenGameViewModel", "Lỗi nghiêm trọng khi tải danh sách động vật: ", e)
            }
        }
    }

    // Hàm công khai để tạo câu hỏi mới (sẽ được gọi khi người dùng muốn chơi tiếp)
    fun generateNewQuestion() {
        _gameState.value = IndenGameState.Loading

        // Đảm bảo không có lỗi nếu danh sách bị rỗng vì lý do nào đó
        if (allAnimalsForGame.size < 4) {
            _gameState.value = IndenGameState.Error
            return
        }

        // 1. Lấy ngẫu nhiên 4 con vật không trùng lặp từ danh sách
        val randomAnimals = allAnimalsForGame.shuffled().take(4)

        // 2. Chọn con đầu tiên trong danh sách ngẫu nhiên đó làm đáp án đúng
        val correctAnswer = randomAnimals[0]

        // 3. Tạo danh sách các đáp án (là tên tiếng Việt) và xáo trộn chúng
        val options = randomAnimals.mapNotNull { it.nameVn }.shuffled()

        // 4. Tạo đối tượng câu hỏi hoàn chỉnh
        val newQuestion = IndenQuizQuestion(
            correctAnimal = correctAnswer,
            options = options
        )

        // 5. Cập nhật state để giao diện hiển thị câu hỏi mới
        _gameState.value = IndenGameState.Success(newQuestion)
    }
}
