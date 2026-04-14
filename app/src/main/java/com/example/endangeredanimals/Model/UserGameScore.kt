package com.example.endangeredanimals.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserGameScore(
    val id: Long? = null,
    @SerialName("userId") val userId: String,
    @SerialName("gameId") val gameId: Long,
    @SerialName("pointsEarned") val pointsEarned: Int,
    @SerialName("playedAt") val playedAt: String? = null
)