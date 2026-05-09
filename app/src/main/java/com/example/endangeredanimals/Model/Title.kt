package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class Title(
    val titleId: Int,
    val name: String,
    val description: String?,
    val minPoints: Int?,
    val iconUrl: String?
)