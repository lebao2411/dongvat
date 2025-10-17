package com.example.endangeredanimals.Model

data class OtpCode(
    val otpId: String = "",
    val userID: String = "",
    val otpCode: String = "",
    val time: Long = 0L, // Thời gian hết hạn
    val isUsed: Boolean = false
)