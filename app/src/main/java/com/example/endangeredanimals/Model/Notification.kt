package com.example.endangeredanimals.Model

data class Notification(
    val notificationId: Long,
    val accountId: String?,
    val title: String?,
    val body: String?,
    val isRead: Boolean?,
    val createdAt: String?
)