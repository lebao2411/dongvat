package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val notificationId: Long,
    val accountId: String?,
    val title: String?,
    val body: String?,
    val isRead: Boolean?,
    val createdAt: String?
)