package com.example.endangeredanimals.Model

data class QuizOption(
    val optionID: String = "",
    val quizID: String = "",
    val isCorrect: Boolean = false,
    val optionText: String = "",
    val optionAnimalID: String = "" // ID của con vật nếu câu trả lời là hình ảnh
)