package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Contribution(
    val contributionId: String,
    val accountId: String?,
    val imageUrl: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val aiPrediction: JsonElement? = null,
    val status: String?,
    val userNote: String? = null,
    val finalAnimalId: String? = null,
    val createdAt: String?
)