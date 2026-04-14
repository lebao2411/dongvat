package com.example.endangeredanimals.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoryNode(
    val id: Long,
    @SerialName("gameId") val gameId: Long,
    val content: String,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("isStartNode") val isStartNode: Boolean = false
)