package com.example.endangeredanimals.Model

data class Contribution(
    val contributionId: String,
    val accountId: String?,
    val imageUrl: String,
    val latitude: Double?,
    val longitude: Double?,
    val aiPrediction: String?,
    val status: String?,
    val userNote: String?,
    val finalAnimalId: String?,
    val createdAt: String?
)