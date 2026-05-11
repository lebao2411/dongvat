package com.example.endangeredanimals.View

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

data class AnimalSuggestion(val vnName: String, val sciName: String, val confidence: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussScreen(
    navController: NavController,
    viewModel: DiscussViewModel = viewModel()
) {
    val contributions by viewModel.contributions.collectAsState()
    val currentDiscussions by viewModel.currentDiscussions.collectAsState()
    val voteData by viewModel.voteData.collectAsState()

    // HỨNG TỪ ĐIỂN TỪ VIEWMODEL
    val animalNamesMap by viewModel.animalNamesMap.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    var selectedContribution by remember { mutableStateOf<Contribution?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thảo luận cộng đồng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && contributions.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(contributions) { contribution ->
                        ContributionCard(
                            contribution = contribution,
                            onOpenDiscussion = {
                                viewModel.fetchDiscussionsForContribution(contribution.contributionId)
                                selectedContribution = contribution
                            }
                        )
                    }
                }
            }
        }

        if (selectedContribution != null) {
            DiscussionBottomSheet(
                contribution = selectedContribution!!,
                discussions = currentDiscussions,
                voteDataMap = voteData,
                animalNamesMap = animalNamesMap, // TRUYỀN TỪ ĐIỂN VÀO BOTTOM SHEET
                viewModel = viewModel,
                onDismiss = { selectedContribution = null }
            )
        }
    }
}

@Composable
fun ContributionCard(contribution: Contribution, onOpenDiscussion: () -> Unit) {
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
                    Text("Người dùng ẩn danh", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    animalNamesMap: Map<String, String>, // NHẬN TỪ ĐIỂN
    viewModel: DiscussViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(horizontal = 16.dp)) {
            Text("Đóng góp ý kiến", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(discussions) { discussion ->
                    if (discussion.accountId == "SYSTEM_AI") {
                        AiSystemCommentCard(
                            comment = discussion,
                            voteData = voteDataMap[discussion.discussionId],
                            onVote = { isLike -> viewModel.toggleVote(discussion.discussionId, isLike) }
                        )
                    } else {
                        CommentItem(
                            discussion = discussion,
                            voteData = voteDataMap[discussion.discussionId],
                            animalNamesMap = animalNamesMap, // TRUYỀN XUỐNG TỪNG COMMENT
                            onVote = { isLike -> viewModel.toggleVote(discussion.discussionId, isLike) }
                        )
                    }
                }
            }

            CommentInputArea(
                aiJsonString = contribution.aiPrediction?.toString(),
                onSend = { text, sciName, vnName ->
                    viewModel.sendComment(contribution.contributionId, text, sciName, vnName) { errorMsg ->
                        if (errorMsg != null) {
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    voteData: VoteData?,
    animalNamesMap: Map<String, String>, // NHẬN TỪ ĐIỂN
    onVote: (Boolean?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thành viên", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = discussion.comment, fontSize = 15.sp)

            // DỊCH ID THÀNH TÊN TIẾNG VIỆT Ở ĐÂY
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

            CommentActionRow(initialData = voteData, onVote = onVote)
        }
    }
}

@Composable
fun CommentInputArea(aiJsonString: String?, onSend: (String, String?, String?) -> Unit) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        if (selectedAnimal != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(selectedAnimal!!.vnName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(selectedAnimal!!.sciName, fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { selectedAnimal = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, "Xóa")
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Box {
                IconButton(onClick = { expandedDropdown = true }) {
                    Icon(Icons.Default.AddCircleOutline, "Đề xuất loài", tint = MaterialTheme.colorScheme.primary)
                }

                DropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                    if (aiSuggestions.isEmpty()) {
                        DropdownMenuItem(text = { Text("Không có đề xuất AI") }, onClick = { expandedDropdown = false })
                    } else {
                        aiSuggestions.forEach { animal ->
                            DropdownMenuItem(
                                text = { Text("${animal.vnName} - AI: ${animal.confidence}%") },
                                onClick = {
                                    selectedAnimal = animal
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Thêm ý kiến...") },
                modifier = Modifier.weight(1f).padding(bottom = 4.dp),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    // TRUYỀN CẢ sciName VÀ vnName LÊN VIEWMODEL
                    onSend(inputText, selectedAnimal?.sciName, selectedAnimal?.vnName)
                    inputText = ""
                    selectedAnimal = null
                },
                enabled = inputText.isNotBlank() || selectedAnimal != null
            ) {
                Icon(Icons.Default.Send, "Gửi", tint = if (inputText.isNotBlank() || selectedAnimal != null) MaterialTheme.colorScheme.primary else Color.Gray)
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