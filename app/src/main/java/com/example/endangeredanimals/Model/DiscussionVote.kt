package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable

@Serializable
data class DiscussionVote(
    val voteId: Long, // int8
    val discussionId: Long?,
    val accountId: String?,
    val voteType: String?
)

// THÊM CLASS NÀY: Dùng để đẩy dữ liệu lên (Bỏ qua voteId để Supabase tự sinh ra)
@Serializable
data class DiscussionVoteInsert(
    val discussionId: Long?,
    val accountId: String?,
    val voteType: String?
)