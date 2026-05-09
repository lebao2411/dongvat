package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class Contribution(
    val contributionId: String,
    val accountId: String?,
    val imageUrl: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val aiPrediction: String? = null,
    val status: String?,
    val userNote: String? = null,
    val finalAnimalId: String? = null,
    val createdAt: String?
)