package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class CommunityDiscussion(
    val discussionId: Long,
    val contributionId: String?,
    val accountId: String?,
    val comment: String,
    val suggestedAnimalId: String?,
    val createdAt: String?
)