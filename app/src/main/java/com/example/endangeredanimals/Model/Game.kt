package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Game(
    val id: Long,
    @SerialName("animalId") val animalId: String?,
    val title: String,
    val description: String? = null,
    @SerialName("game_type") val gameType: String,
    val difficulty: String = "Dễ"
)
