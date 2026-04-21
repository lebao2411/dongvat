package com.example.endangeredanimals.Model

data class PointLog(
    val logId: Long,
    val accountId: String,
    val actionType: String,
    val points: Int,
    val referenceId: String?,
    val createdAt: String?
)