package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Account(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("userName")
    val userName: String = "",
    val email: String = "",
    val password: String? = null,
    val habitatScore: Int = 0,
    val conservationScore: Int = 0
)
