package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Favorite(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("animalId")
    val animalId: String = ""
)
