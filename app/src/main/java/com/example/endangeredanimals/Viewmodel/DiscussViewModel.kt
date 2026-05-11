package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.CommunityDiscussion
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.Model.DiscussionVote
import com.example.endangeredanimals.Model.DiscussionVoteInsert
import com.example.endangeredanimals.Model.CommunityDiscussionInsert
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VoteState { LIKE, DISLIKE, NONE }
data class VoteData(val likes: Int, val dislikes: Int, val userVote: VoteState)

class DiscussViewModel : ViewModel() {

    private val _contributions = MutableStateFlow<List<Contribution>>(emptyList())
    val contributions = _contributions.asStateFlow()

    private val _currentDiscussions = MutableStateFlow<List<CommunityDiscussion>>(emptyList())
    val currentDiscussions = _currentDiscussions.asStateFlow()

    private val _voteData = MutableStateFlow<Map<Long, VoteData>>(emptyMap())
    val voteData = _voteData.asStateFlow()

    // TỪ ĐIỂN DỊCH ID ĐỘNG VẬT -> TÊN TIẾNG VIỆT
    private val _animalNamesMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val animalNamesMap = _animalNamesMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init { fetchDiscussingContributions() }

    fun fetchDiscussingContributions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = SupabaseInstance.client.from("contributions")
                    .select { filter { eq("status", "discussing") } }
                    .decodeList<Contribution>()
                _contributions.value = result
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun fetchDiscussionsForContribution(contributionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val discussions = SupabaseInstance.client.from("community_discussions")
                    .select { filter { eq("contributionId", contributionId) } }
                    .decodeList<CommunityDiscussion>()
                    .sortedWith(compareByDescending<CommunityDiscussion> { it.accountId == "SYSTEM_AI" }
                        .thenBy { it.createdAt })

                _currentDiscussions.value = discussions
                discussions.forEach { fetchVotesForDiscussion(it.discussionId) }

                // --- BẮT ĐẦU DỊCH ID THÀNH TÊN TIẾNG VIỆT ---
                val animalIds = discussions.mapNotNull { it.suggestedAnimalId }.filter { it.isNotBlank() }.distinct()
                if (animalIds.isNotEmpty()) {
                    val animals = SupabaseInstance.client.from("animals")
                        // Đã sửa thành "animalId" (chữ d thường) để khớp với Database Supabase
                        .select { filter { isIn("animalId", animalIds) } }
                        .decodeList<com.example.endangeredanimals.Model.Animal>()

                    // Đã dùng chính xác biến animalID và nameVn từ Model Animal của bạn
                    val nameMap = animals.filter { it.animalID != null }.associate {
                        it.animalID!! to (it.nameVn ?: "Loài chưa xác định")
                    }
                    _animalNamesMap.value = nameMap
                }
                // ---------------------------------------------

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchVotesForDiscussion(discussionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val votes = SupabaseInstance.client.from("discussion_votes")
                    .select { filter { eq("discussionId", discussionId) } }
                    .decodeList<DiscussionVote>()

                val userId = SupabaseInstance.client.auth.currentSessionOrNull()?.user?.id
                val likes = votes.count { it.voteType?.lowercase() == "like" }
                val dislikes = votes.count { it.voteType?.lowercase() == "dislike" }
                val myVoteStr = votes.find { it.accountId == userId }?.voteType

                val myVote = when(myVoteStr?.lowercase()) {
                    "like" -> VoteState.LIKE
                    "dislike" -> VoteState.DISLIKE
                    else -> VoteState.NONE
                }

                _voteData.value = _voteData.value + (discussionId to VoteData(likes, dislikes, myVote))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sendComment(contributionId: String, text: String, sciName: String?, vnName: String?, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = SupabaseInstance.client.auth.currentSessionOrNull()?.user
            val fakeId = System.currentTimeMillis()

            try {
                var finalRealAnimalId: String? = null

                // 1. Nếu có đề xuất, phải lấy ID thật từ DB trước
                if (sciName != null) {
                    try {
                        val animals = SupabaseInstance.client.from("animals")
                            .select { filter { ilike("nameLatin", "%$sciName%") } }
                            .decodeList<com.example.endangeredanimals.Model.Animal>()

                        val foundId = animals.firstOrNull()?.animalID
                        if (foundId != null) {
                            finalRealAnimalId = foundId

                            // Nạp ngay tên Tiếng Việt vào Từ điển để hiển thị lập tức
                            if (vnName != null) {
                                _animalNamesMap.value = _animalNamesMap.value + (finalRealAnimalId to vnName)
                            }
                        } else {
                            throw Exception("LOAI_KHONG_HOP_LE")
                        }
                    } catch (e: Exception) {
                        if (e.message == "LOAI_KHONG_HOP_LE") throw e
                    }
                }

                // 2. Chèn tạm lên màn hình bằng ID THẬT
                val tempComment = CommunityDiscussion(
                    discussionId = fakeId,
                    contributionId = contributionId,
                    accountId = user?.id ?: "unknown",
                    comment = text,
                    suggestedAnimalId = finalRealAnimalId,
                    createdAt = null
                )

                withContext(Dispatchers.Main) {
                    val currentList = _currentDiscussions.value.toMutableList()
                    val insertIndex = currentList.indexOfLast { it.accountId == "SYSTEM_AI" } + 1
                    currentList.add(if (insertIndex > 0) insertIndex else 0, tempComment)
                    _currentDiscussions.value = currentList
                    onComplete(null) // Xóa rỗng ô nhập liệu
                }

                // 3. Đẩy lên Supabase
                val newCommentToDB = CommunityDiscussionInsert(
                    contributionId = contributionId,
                    accountId = user?.id,
                    comment = text,
                    suggestedAnimalId = finalRealAnimalId
                )
                SupabaseInstance.client.from("community_discussions").insert(newCommentToDB)
                fetchDiscussionsForContribution(contributionId)

            } catch (e: Exception) {
                e.printStackTrace()
                fetchDiscussionsForContribution(contributionId)
                withContext(Dispatchers.Main) {
                    if (e.message == "LOAI_KHONG_HOP_LE" || e.message?.contains("foreign key constraint") == true) {
                        onComplete("Đề xuất thất bại: Loài này chưa có trong Sách Đỏ Việt Nam!")
                    } else {
                        onComplete("Có lỗi xảy ra khi gửi bình luận.")
                    }
                }
            }
        }
    }

    fun toggleVote(discussionId: Long, isLike: Boolean?) {
        if (discussionId > 1_000_000_000_000L) {
            Log.w("VoteTest", "Bình luận đang đồng bộ, chưa có ID thật để Vote!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = SupabaseInstance.client.auth.currentSessionOrNull()?.user?.id ?: return@launch

                SupabaseInstance.client.from("discussion_votes").delete {
                    filter { eq("discussionId", discussionId); eq("accountId", userId) }
                }

                if (isLike != null) {
                    val type = if (isLike) "like" else "dislike"
                    val newVote = DiscussionVoteInsert(discussionId, userId, type)
                    SupabaseInstance.client.from("discussion_votes").insert(newVote)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}