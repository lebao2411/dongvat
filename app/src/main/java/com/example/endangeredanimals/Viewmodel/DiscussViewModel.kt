package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.CommunityDiscussion
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.Model.DiscussionVote
import com.example.endangeredanimals.Model.DiscussionVoteInsert
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class lưu trữ trạng thái Vote của từng Bình luận
enum class VoteState { LIKE, DISLIKE, NONE }
data class VoteData(val likes: Int, val dislikes: Int, val userVote: VoteState)

class DiscussViewModel : ViewModel() {

    private val _contributions = MutableStateFlow<List<Contribution>>(emptyList())
    val contributions = _contributions.asStateFlow()

    private val _currentDiscussions = MutableStateFlow<List<CommunityDiscussion>>(emptyList())
    val currentDiscussions = _currentDiscussions.asStateFlow()

    private val _voteData = MutableStateFlow<Map<Long, VoteData>>(emptyMap())
    val voteData = _voteData.asStateFlow()

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
                val likes = votes.count { it.voteType == "LIKE" }
                val dislikes = votes.count { it.voteType == "DISLIKE" }
                val myVoteStr = votes.find { it.accountId == userId }?.voteType

                val myVote = when(myVoteStr) {
                    "LIKE" -> VoteState.LIKE
                    "DISLIKE" -> VoteState.DISLIKE
                    else -> VoteState.NONE
                }

                _voteData.value = _voteData.value + (discussionId to VoteData(likes, dislikes, myVote))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // YÊU CẦU 3: OPTIMISTIC UI KHI GỬI COMMENT
    fun sendComment(contributionId: String, text: String, suggestedAnimalId: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = SupabaseInstance.client.auth.currentSessionOrNull()?.user
            val fakeId = System.currentTimeMillis() // Tạo ID giả để hiển thị ngay lập tức

            val tempComment = CommunityDiscussion(
                discussionId = fakeId,
                contributionId = contributionId,
                accountId = user?.id ?: "unknown",
                comment = text,
                suggestedAnimalId = suggestedAnimalId,
                createdAt = null
            )

            // Chèn ngay bình luận mới vào sau bình luận của AI trên màn hình
            val currentList = _currentDiscussions.value.toMutableList()
            val insertIndex = currentList.indexOfLast { it.accountId == "SYSTEM_AI" } + 1
            currentList.add(if (insertIndex > 0) insertIndex else 0, tempComment)
            _currentDiscussions.value = currentList

            // Xóa rỗng textfield ngay lập tức cho mượt
            withContext(Dispatchers.Main) { onComplete() }

            // Chạy ngầm việc lưu vào DB phía sau
            withContext(Dispatchers.IO) {
                try {
                    SupabaseInstance.client.from("community_discussions").insert(
                        CommunityDiscussion(0, contributionId, user?.id, text, suggestedAnimalId, null)
                    )
                    // Lưu thành công thì tải lại để lấy ID thật
                    fetchDiscussionsForContribution(contributionId)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // YÊU CẦU 2: XỬ LÝ VOTE CÓ CẬP NHẬT DB NGẦM
    fun toggleVote(discussionId: Long, isLike: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = SupabaseInstance.client.auth.currentSessionOrNull()?.user?.id ?: return@launch

                // Bước 1: Xóa trắng Vote cũ của User này cho bình luận này (nếu có)
                SupabaseInstance.client.from("discussion_votes").delete {
                    filter {
                        eq("discussionId", discussionId)
                        eq("accountId", userId)
                    }
                }

                // Bước 2: Thêm Vote mới bằng Model Insert (Không truyền số 0 nữa)
                if (isLike != null) {
                    val type = if (isLike) "LIKE" else "DISLIKE"

                    // SỬ DỤNG DiscussionVoteInsert Ở ĐÂY
                    val newVote = DiscussionVoteInsert(
                        discussionId = discussionId,
                        accountId = userId,
                        voteType = type
                    )
                    SupabaseInstance.client.from("discussion_votes").insert(newVote)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Nếu có lỗi nó sẽ in ra Logcat của Android Studio
            }
        }
    }
}