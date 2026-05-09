package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class PointLog(
    val logId: Long,
    val accountId: String,
    val actionType: String,
    val points: Int,
    val referenceId: String?,
    val createdAt: String?
)