package com.example.endangeredanimals.View

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.endangeredanimals.Model.CommunityDiscussion
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.DiscussViewModel
import com.example.endangeredanimals.ViewModel.VoteData
import com.example.endangeredanimals.ViewModel.VoteState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

data class AnimalSuggestion(val vnName: String, val sciName: String, val confidence: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussScreen(
    navController: NavController,
    viewModel: DiscussViewModel = viewModel(),
    initialContributionId: String? = null
) {
    val contributions by viewModel.contributions.collectAsState()
    val currentDiscussions by viewModel.currentDiscussions.collectAsState()
    val voteData by viewModel.voteData.collectAsState()

    val animalNamesMap by viewModel.animalNamesMap.collectAsState()
    val allAnimals by viewModel.allAnimals.collectAsState()
    val userNamesMap by viewModel.userNamesMap.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    var selectedContribution by remember { mutableStateOf<Contribution?>(null) }

    LaunchedEffect(initialContributionId, contributions) {
        if (initialContributionId != null && contributions.isNotEmpty() && selectedContribution == null) {
            val targetContribution = contributions.find { it.contributionId == initialContributionId }
            if (targetContribution != null) {
                viewModel.fetchDiscussionsForContribution(initialContributionId)
                selectedContribution = targetContribution
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (initialContributionId != null) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        if (isLoading && contributions.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (initialContributionId == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Thảo luận cộng đồng",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(contributions) { contribution ->
                            val authorName = userNamesMap[contribution.accountId] ?: "Người dùng ẩn danh"

                            ContributionCard(
                                contribution = contribution,
                                userName = authorName,
                                onOpenDiscussion = {
                                    viewModel.fetchDiscussionsForContribution(contribution.contributionId)
                                    selectedContribution = contribution
                                }
                            )
                        }
                    }
                }
            }
        }

        if (selectedContribution != null) {
            DiscussionBottomSheet(
                contribution = selectedContribution!!,
                discussions = currentDiscussions,
                voteDataMap = voteData,
                animalNamesMap = animalNamesMap,
                userNamesMap = userNamesMap,
                allAnimals = allAnimals,
                viewModel = viewModel,
                onDismiss = {
                    selectedContribution = null
                    if (initialContributionId != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@Composable
private fun ContributionCard(
    contribution: Contribution,
    userName: String,
    onOpenDiscussion: () -> Unit
) {
    val timeString = remember(contribution.createdAt) {
        try {
            if (contribution.createdAt == null) "Vừa xong" else {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                formatter.format(parser.parse(contribution.createdAt)!!)
            }
        } catch (e: Exception) { "Gần đây" }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(timeString, fontSize = 12.sp, color = Color.Gray)
                }
            }

            AsyncImage(
                model = contribution.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contribution.userNote ?: "Cần hỗ trợ định danh loài này!",
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp
                )
                TextButton(onClick = onOpenDiscussion) {
                    Icon(painterResource(R.drawable.chat), null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thảo luận", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionBottomSheet(
    contribution: Contribution,
    discussions: List<CommunityDiscussion>,
    voteDataMap: Map<Long, VoteData>,
    animalNamesMap: Map<String, String>,
    allAnimals: List<com.example.endangeredanimals.Model.Animal>,
    userNamesMap: Map<String, String>,
    viewModel: DiscussViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    // Ẩn thanh trạng thái khi mở BottomSheet và hiện lại khi đóng
    DisposableEffect(Unit) {
        systemUiController.isStatusBarVisible = false
        onDispose {
            systemUiController.isStatusBarVisible = true
        }
    }

    var replyingTo by remember { mutableStateOf<CommunityDiscussion?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Text("Đóng góp ý kiến", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                // ĐÃ THÊM: Phần hiển thị Ảnh và Lời nhắn ở trên cùng BottomSheet
                item {
                    ContributionContextCard(contribution = contribution)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                }

                items(discussions) { discussion ->
                    if (discussion.accountId == "SYSTEM_AI") {
                        AiSystemCommentCard(
                            comment = discussion,
                            voteData = voteDataMap[discussion.discussionId],
                            onVote = { isLike -> viewModel.toggleVote(discussion.discussionId, isLike) }
                        )
                    } else {
                        val authorName = userNamesMap[discussion.accountId] ?: "Thành viên"
                        CommentItem(
                            discussion = discussion,
                            userName = authorName,
                            voteData = voteDataMap[discussion.discussionId],
                            animalNamesMap = animalNamesMap,
                            onVote = { isLike -> viewModel.toggleVote(discussion.discussionId, isLike) },
                            onReply = {
                                replyingTo = if (discussion.parentId != null) {
                                    discussions.find { it.discussionId == discussion.parentId } ?: discussion
                                } else {
                                    discussion
                                }
                            }
                        )
                    }
                }
            }

            val replyingToName = replyingTo?.accountId?.let { userNamesMap[it] }
            CommentInputArea(
                aiJsonString = contribution.aiPrediction?.toString(),
                allAnimals = allAnimals,
                replyingToName = replyingToName,
                onCancelReply = { replyingTo = null },
                onSend = { text, sciName, vnName ->
                    viewModel.sendComment(
                        contributionId = contribution.contributionId,
                        text = text,
                        sciName = sciName,
                        vnName = vnName,
                        parentId = replyingTo?.discussionId
                    ) { errorMsg ->
                        if (errorMsg != null) {
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                    replyingTo = null
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ĐÃ THÊM: Giao diện thẻ hiển thị ngữ cảnh bài viết trong BottomSheet
@Composable
fun ContributionContextCard(contribution: Contribution) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            AsyncImage(
                model = contribution.imageUrl,
                contentDescription = "Ảnh bài viết",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Cố định chiều cao để không chiếm hết màn hình
                    .background(Color.Black),
                contentScale = ContentScale.Crop
            )

            if (!contribution.userNote.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quote",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contribution.userNote,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun AiSystemCommentCard(
    comment: CommunityDiscussion,
    voteData: VoteData?,
    onVote: (Boolean?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hệ thống AI Dự Đoán", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))

            Text(text = comment.comment, fontSize = 15.sp, fontWeight = FontWeight.Medium)

            CommentActionRow(initialData = voteData, onVote = onVote)
        }
    }
}

@Composable
fun CommentItem(
    discussion: CommunityDiscussion,
    userName: String,
    voteData: VoteData?,
    animalNamesMap: Map<String, String>,
    onVote: (Boolean?) -> Unit,
    onReply: () -> Unit
) {
    val paddingStart = if (discussion.parentId != null) 36.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 6.dp, bottom = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (discussion.parentId != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = discussion.comment, fontSize = 14.sp)

            if (!discussion.suggestedAnimalId.isNullOrBlank()) {
                val displayName = animalNamesMap[discussion.suggestedAnimalId] ?: "Đang tải tên loài..."
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Đề xuất: $displayName", fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trả lời",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onReply() }
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                CommentActionRow(initialData = voteData, onVote = onVote)
            }
        }
    }
}

@Composable
fun CommentInputArea(
    aiJsonString: String?,
    allAnimals: List<com.example.endangeredanimals.Model.Animal>,
    replyingToName: String?,
    onCancelReply: () -> Unit,
    onSend: (String, String?, String?) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var selectedAnimal by remember { mutableStateOf<AnimalSuggestion?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val aiSuggestions = remember(aiJsonString) {
        val list = mutableListOf<AnimalSuggestion>()
        if (!aiJsonString.isNullOrBlank()) {
            try {
                val arr = JSONArray(aiJsonString)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val species = obj.getString("speciesName")
                    val vnName = species.substringBefore(" (").trim()
                    val sciName = species.substringAfter(" (").removeSuffix(")").trim()
                    val conf = obj.getInt("confidence")
                    list.add(AnimalSuggestion(vnName, sciName, conf))
                }
            } catch (e: Exception) {}
        }
        list
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        if (replyingToName != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Đang trả lời bình luận của ", fontSize = 12.sp, color = Color.Gray)
                    Text(replyingToName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hủy trả lời", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 100.dp)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text("Thêm ý kiến...", color = Color.Gray, fontSize = 15.sp)
                        }
                        innerTextField()
                    }
                )

                if (selectedAnimal != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Default.Pets, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedAnimal!!.vnName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(selectedAnimal!!.sciName, fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { selectedAnimal = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, "Xóa", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { expandedDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chọn loài theo ý kiến",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Mở danh sách",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.heightIn(max = 300.dp).background(Color.White)
                        ) {
                            if (aiSuggestions.isNotEmpty()) {
                                Text(
                                    text = "AI ĐỀ XUẤT",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                aiSuggestions.forEach { animal ->
                                    DropdownMenuItem(
                                        text = { Text("✨ ${animal.vnName} (${animal.sciName}) - Khớp: ${animal.confidence}%") },
                                        onClick = {
                                            selectedAnimal = animal
                                            expandedDropdown = false
                                        }
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }

                            Text(
                                text = "TẤT CẢ CÁC LOÀI",
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            if (allAnimals.isEmpty()) {
                                DropdownMenuItem(text = { Text("Đang tải danh sách...") }, onClick = {})
                            } else {
                                allAnimals.forEach { animal ->
                                    val vnName = animal.nameVn ?: "Chưa rõ"
                                    val sciName = animal.nameLatin ?: ""
                                    val displayText = if (sciName.isNotBlank()) "$vnName ($sciName)" else vnName

                                    DropdownMenuItem(
                                        text = { Text(displayText) },
                                        onClick = {
                                            selectedAnimal = AnimalSuggestion(
                                                vnName = vnName,
                                                sciName = sciName,
                                                confidence = 0
                                            )
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            onSend(inputText, selectedAnimal?.sciName, selectedAnimal?.vnName)
                            inputText = ""
                            selectedAnimal = null
                        },
                        enabled = inputText.isNotBlank() || selectedAnimal != null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gửi",
                            tint = if (inputText.isNotBlank() || selectedAnimal != null) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentActionRow(initialData: VoteData?, onVote: (Boolean?) -> Unit) {
    var localLikes by remember(initialData) { mutableStateOf(initialData?.likes ?: 0) }
    var localDislikes by remember(initialData) { mutableStateOf(initialData?.dislikes ?: 0) }
    var localVote by remember(initialData) { mutableStateOf(initialData?.userVote ?: VoteState.NONE) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (localVote == VoteState.LIKE) {
                localVote = VoteState.NONE; localLikes--
                onVote(null)
            } else {
                if (localVote == VoteState.DISLIKE) localDislikes--
                localVote = VoteState.LIKE; localLikes++
                onVote(true)
            }
        }) {
            Icon(
                imageVector = if (localVote == VoteState.LIKE) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Like", modifier = Modifier.size(18.dp),
                tint = if (localVote == VoteState.LIKE) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
        Text("$localLikes", fontSize = 13.sp, color = if (localVote == VoteState.LIKE) MaterialTheme.colorScheme.primary else Color.Black)

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = {
            if (localVote == VoteState.DISLIKE) {
                localVote = VoteState.NONE; localDislikes--
                onVote(null)
            } else {
                if (localVote == VoteState.LIKE) localLikes--
                localVote = VoteState.DISLIKE; localDislikes++
                onVote(false)
            }
        }) {
            Icon(
                imageVector = if (localVote == VoteState.DISLIKE) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Dislike", modifier = Modifier.size(18.dp),
                tint = if (localVote == VoteState.DISLIKE) Color(0xFFE53935) else Color.Gray
            )
        }
        Text("$localDislikes", fontSize = 13.sp, color = if (localVote == VoteState.DISLIKE) Color(0xFFE53935) else Color.Black)
    }
}