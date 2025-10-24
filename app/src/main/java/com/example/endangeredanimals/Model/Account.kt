package com.example.endangeredanimals.Model

data class Account(
    val userId: String = "",
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val habitatScore: Int = 0,
    val conservationScore: Int = 0
)