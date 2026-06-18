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
import com.example.endangeredanimals.Model.PointLogInsert
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
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

    private val _userNamesMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNamesMap = _userNamesMap.asStateFlow()

    init {
        val user = SupabaseInstance.client.auth.currentSessionOrNull()?.user
        if (user != null) {
            fetchDiscussingContributions()
        } else {
            _contributions.value = emptyList()
            _isLoading.value = false
        }
    }

    fun fetchDiscussingContributions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = SupabaseInstance.client.from("contributions")
                    .select { filter { eq("status", "discussing") } }
                    .decodeList<Contribution>()
                _contributions.value = result

                // MỚI THÊM: Lấy danh sách tên người dùng
                val accountIds = result.mapNotNull { it.accountId }.distinct()
                if (accountIds.isNotEmpty()) {
                    val accounts = SupabaseInstance.client.from("accounts")
                        .select { filter { isIn("userId", accountIds) } }
                        .decodeList<com.example.endangeredanimals.Model.Account>()

                    val nameMap = accounts.associate { it.userId to it.userName }
                    _userNamesMap.value = nameMap
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun fetchDiscussionsForContribution(contributionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _voteData.value = emptyMap()

                val rawDiscussions = SupabaseInstance.client.from("community_discussions")
                    .select { filter { eq("contributionId", contributionId) } }
                    .decodeList<CommunityDiscussion>()

                // --- LOGIC SẮP XẾP LỒNG NHAU ---
                // 1. Bình luận của AI luôn ở trên cùng
                val aiComments = rawDiscussions.filter { it.accountId == "SYSTEM_AI" }.sortedBy { it.createdAt }
                // 2. Bình luận gốc của User
                val userParents = rawDiscussions.filter { it.accountId != "SYSTEM_AI" && it.parentId == null }.sortedBy { it.createdAt }
                // 3. Gom nhóm các câu trả lời theo parentId
                val userChildren = rawDiscussions.filter { it.parentId != null }.groupBy { it.parentId }

                val finalDisplayList = mutableListOf<CommunityDiscussion>()
                finalDisplayList.addAll(aiComments)

                // Lắp ráp: Cứ 1 bình luận cha thì kéo theo các bình luận con của nó
                for (parent in userParents) {
                    finalDisplayList.add(parent)
                    userChildren[parent.discussionId]?.let { children ->
                        finalDisplayList.addAll(children.sortedBy { it.createdAt })
                    }
                }

                _currentDiscussions.value = finalDisplayList
                finalDisplayList.forEach { fetchVotesForDiscussion(it.discussionId) }

                // --- DỊCH ID THÀNH TÊN TIẾNG VIỆT ---
                val animalIds = rawDiscussions.mapNotNull { it.suggestedAnimalId }.filter { it.isNotBlank() }.distinct()
                if (animalIds.isNotEmpty()) {
                    val animals = SupabaseInstance.client.from("animals")
                        .select { filter { isIn("animalId", animalIds) } }
                        .decodeList<com.example.endangeredanimals.Model.Animal>()

                    val nameMap = animals.filter { it.animalID != null }.associate {
                        it.animalID!! to (it.nameVn ?: "Loài chưa xác định")
                    }
                    _animalNamesMap.value = nameMap
                }
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

    fun sendComment(contributionId: String, text: String, sciName: String?, vnName: String?, parentId: Long? = null, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = SupabaseInstance.client.auth.currentSessionOrNull()?.user
            val fakeId = System.currentTimeMillis()

            try {
                var finalRealAnimalId: String? = null
                if (sciName != null) {
                    try {
                        val animals = SupabaseInstance.client.from("animals")
                            .select { filter { ilike("nameLatin", "%$sciName%") } }
                            .decodeList<com.example.endangeredanimals.Model.Animal>()

                        val foundId = animals.firstOrNull()?.animalID
                        if (foundId != null) {
                            finalRealAnimalId = foundId
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

                // Cập nhật chèn tạm thời
                val tempComment = CommunityDiscussion(
                    discussionId = fakeId,
                    contributionId = contributionId,
                    accountId = user?.id ?: "unknown",
                    comment = text,
                    suggestedAnimalId = finalRealAnimalId,
                    createdAt = null,
                    parentId = parentId // Lưu parentId
                )

                withContext(Dispatchers.Main) {
                    val currentList = _currentDiscussions.value.toMutableList()
                    currentList.add(tempComment)
                    _currentDiscussions.value = currentList
                    onComplete(null)
                }

                // Đẩy lên Supabase
                val newCommentToDB = CommunityDiscussionInsert(
                    contributionId = contributionId,
                    accountId = user?.id,
                    comment = text,
                    suggestedAnimalId = finalRealAnimalId,
                    parentId = parentId // Ghi vào DB
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
        if (discussionId > 1_000_000_000_000L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = SupabaseInstance.client.auth.currentSessionOrNull()?.user?.id ?: return@launch

                // 1. LẤY TRẠNG THÁI HIỆN TẠI TỪ LOCAL ĐỂ XỬ LÝ NHANH
                val oldVoteData = _voteData.value[discussionId]
                val oldVoteState = oldVoteData?.userVote ?: VoteState.NONE
                
                val nextVoteType = if (isLike == true) "like" else if (isLike == false) "dislike" else null
                val oldVoteStr = when(oldVoteState) {
                    VoteState.LIKE -> "like"
                    VoteState.DISLIKE -> "dislike"
                    else -> null
                }

                // Nếu nhấn lại chính nút đang chọn -> Tắt vote (Toggle off)
                val finalVoteType = if (oldVoteStr == nextVoteType) null else nextVoteType

                // 2. CẬP NHẬT BẢNG VOTE TRÊN SUPABASE (Xóa cũ - Ghi mới)
                SupabaseInstance.client.from("discussion_votes").delete {
                    filter { eq("discussionId", discussionId); eq("accountId", userId) }
                }

                if (finalVoteType != null) {
                    val newVote = DiscussionVoteInsert(discussionId, userId, finalVoteType)
                    SupabaseInstance.client.from("discussion_votes").insert(newVote)
                }

                // 3. LOGIC TÍNH ĐIỂM (Dựa trên sự thay đổi trạng thái Like thật sự)
                val targetComment = _currentDiscussions.value.find { it.discussionId == discussionId }
                val authorId = targetComment?.accountId

                if (authorId != null && authorId != "SYSTEM_AI" && authorId != userId) {
                    val wasLiked = oldVoteState == VoteState.LIKE
                    val isNowLiked = finalVoteType == "like"

                    val pointsToChange = when {
                        !wasLiked && isNowLiked -> 3   // Từ Không Like -> Like: +3
                        wasLiked && !isNowLiked -> -3  // Từ Like -> Không Like: -3
                        else -> 0                     // Các trường hợp khác: 0
                    }

                    if (pointsToChange != 0) {
                        // Ghi log điểm - Supabase Trigger sẽ tự cộng điểm vào accounts
                        val actionType = if (pointsToChange > 0) "LIKE_ACTION" else "UNLIKE_ACTION"
                        val newLog = PointLogInsert(
                            accountId = authorId,
                            actionType = actionType,
                            points = pointsToChange,
                            referenceId = discussionId.toString()
                        )
                        SupabaseInstance.client.from("point_logs").insert(newLog)
                    }
                }
                
                // Cập nhật lại UI sau khi xong
                fetchVotesForDiscussion(discussionId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Thêm state lưu trữ danh sách động vật
    private val _allAnimals = MutableStateFlow<List<com.example.endangeredanimals.Model.Animal>>(emptyList())
    val allAnimals = _allAnimals.asStateFlow()

    init {
        val user = SupabaseInstance.client.auth.currentSessionOrNull()?.user
        if (user != null) {
            fetchDiscussingContributions()
            fetchAllAnimals() // Gọi hàm tải toàn bộ động vật
        } else {
            _contributions.value = emptyList()
            _isLoading.value = false
        }
    }

    // Hàm tải toàn bộ động vật từ DB
    private fun fetchAllAnimals() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val animals = SupabaseInstance.client.from("animals")
                    .select()
                    .decodeList<com.example.endangeredanimals.Model.Animal>()
                    // Sắp xếp theo bảng chữ cái tiếng Việt
                    .sortedBy { it.nameVn ?: "" }
                _allAnimals.value = animals
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}