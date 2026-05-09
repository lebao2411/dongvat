package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.DiscussViewModel
import org.json.JSONArray

enum class VoteState { LIKE, DISLIKE, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussScreen(
    navController: NavController,
    viewModel: DiscussViewModel = viewModel()
) {
    val contributions by viewModel.contributions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedContributionForDiscussion by remember { mutableStateOf<Contribution?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thảo luận cộng đồng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (isLoading && contributions.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && contributions.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchDiscussingContributions() }) {
                        Text("Thử lại")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(contributions) { contribution ->
                        ContributionCard(
                            contribution = contribution,
                            onLike = { /* Xử lý Vote Up */ },
                            onDislike = { /* Xử lý Vote Down */ },
                            onOpenDiscussion = {
                                selectedContributionForDiscussion = contribution
                            }
                        )
                    }
                }
            }
        }

        if (selectedContributionForDiscussion != null) {
            DiscussionBottomSheet(
                onDismiss = { selectedContributionForDiscussion = null },
                discussions = emptyList()
            )
        }
    }
}

@Composable
fun ContributionCard(
    contribution: Contribution,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onOpenDiscussion: () -> Unit
) {
    val aiTop1Name = remember(contribution.aiPrediction) {
        try {
            if (!contribution.aiPrediction.isNullOrBlank()) {
                val jsonArray = JSONArray(contribution.aiPrediction)
                if (jsonArray.length() > 0) {
                    val top1 = jsonArray.getJSONObject(0)
                    top1.getString("speciesName").substringBefore(" (")
                } else "Chưa xác định"
            } else "Người dùng đề xuất"
        } catch (e: Exception) { "Lỗi dữ liệu AI" }
    }

    var likeCount by rememberSaveable(contribution.contributionId) { mutableStateOf((10..50).random()) }
    var dislikeCount by rememberSaveable(contribution.contributionId) { mutableStateOf((0..15).random()) }
    var voteState by rememberSaveable(contribution.contributionId) { mutableStateOf(VoteState.NONE) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = "Dự đoán: $aiTop1Name", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (!contribution.userNote.isNullOrBlank()) {
                        Text(text = contribution.userNote!!, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }

            AsyncImage(
                model = contribution.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // --- CỤM NÚT LIKE MỚI ---
                    IconButton(onClick = {
                        when (voteState) {
                            VoteState.LIKE -> {
                                voteState = VoteState.NONE
                                likeCount--
                            }
                            VoteState.DISLIKE -> {
                                voteState = VoteState.LIKE
                                dislikeCount--
                                likeCount++
                            }
                            VoteState.NONE -> {
                                voteState = VoteState.LIKE
                                likeCount++
                            }
                        }
                        onLike()
                    }) {
                        Icon(
                            // TRÁO ĐỔI ICON TẠI ĐÂY
                            imageVector = if (voteState == VoteState.LIKE) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            modifier = Modifier.size(22.dp), // Chỉnh size to lên một chút cho đẹp
                            tint = if (voteState == VoteState.LIKE) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    Text("$likeCount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (voteState == VoteState.LIKE) MaterialTheme.colorScheme.primary else Color.Black)

                    Spacer(modifier = Modifier.width(16.dp))

                    // --- CỤM NÚT DISLIKE MỚI ---
                    IconButton(onClick = {
                        when (voteState) {
                            VoteState.DISLIKE -> {
                                voteState = VoteState.NONE
                                dislikeCount--
                            }
                            VoteState.LIKE -> {
                                voteState = VoteState.DISLIKE
                                likeCount--
                                dislikeCount++
                            }
                            VoteState.NONE -> {
                                voteState = VoteState.DISLIKE
                                dislikeCount++
                            }
                        }
                        onDislike()
                    }) {
                        Icon(
                            // TRÁO ĐỔI ICON TẠI ĐÂY
                            imageVector = if (voteState == VoteState.DISLIKE) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Dislike",
                            modifier = Modifier.size(22.dp),
                            tint = if (voteState == VoteState.DISLIKE) Color(0xFFE53935) else Color.Gray
                        )
                    }
                    Text("$dislikeCount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (voteState == VoteState.DISLIKE) Color(0xFFE53935) else Color.Black)
                }

                TextButton(onClick = onOpenDiscussion) {
                    Icon(painterResource(R.drawable.chat), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thảo luận", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionBottomSheet(onDismiss: () -> Unit, discussions: List<Any>) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
            Text("Đóng góp ý kiến", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Chưa có thảo luận nào.", color = Color.Gray)
            }

            var inputText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Bạn nghĩ đây là loài gì?...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(onClick = { /* Gửi ý kiến */ }, enabled = inputText.isNotBlank()) {
                        Icon(Icons.Default.Send, null, tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            )
        }
    }
}