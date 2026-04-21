package com.example.endangeredanimals.Model

data class DiscussionVote(
    val voteId: Long, // int8
    val discussionId: Long?,
    val accountId: String?,
    val voteType: String?
)