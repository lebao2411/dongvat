package com.example.endangeredanimals.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoryChoice(
    val id: Long,
    @SerialName("nodeId") val nodeId: Long,
    val text: String,
    @SerialName("nextNodeId") val nextNodeId: Long? = null,
    @SerialName("isCorrect") val isCorrect: Boolean = true,
    @SerialName("scoreReward") val scoreReward: Int = 0,
    @SerialName("isGameOver") val isGameOver: Boolean = false,
    val lesson: String? = null
)