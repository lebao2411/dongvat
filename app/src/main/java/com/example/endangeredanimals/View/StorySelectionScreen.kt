package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.GameWithStatus
import com.example.endangeredanimals.ViewModel.StoryGameViewModel
import com.example.endangeredanimals.ui.AppBackgroundCard
import com.example.endangeredanimals.ui.AppPrimaryColor

@Composable
fun StoryGameScreen(
    navController: NavController,
    viewModel: StoryGameViewModel = viewModel()
) {
    val gamesList by viewModel.gamesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Truyền dữ liệu vào một Composable chỉ lo phần hiển thị
    StoryGameContent(
        gamesList = gamesList,
        isLoading = isLoading,
        onBackClick = { navController.popBackStack() },
        onGameClick = { gameId ->
            navController.navigate("story_play/$gameId")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryGameContent(
    gamesList: List<GameWithStatus>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onGameClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Hành trình sinh tồn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimaryColor)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(gamesList) { item ->
                        GameScenarioCard(
                            title = item.game.title,
                            imageUrl = item.animal?.imageUrl,
                            difficulty = item.game.difficulty,
                            isPlayed = item.isPlayed,
                            onClick = { onGameClick(item.game.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameScenarioCard(
    title: String,
    imageUrl: String?,
    difficulty: String,
    isPlayed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundCard)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl ?: R.drawable.protect_animals)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(R.drawable.loading),
                    error = painterResource(R.drawable.noimage)
                )
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPlayed) Color(0xFF37ab3c) else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPlayed) "Đã chơi" else "Chưa chơi",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Độ khó: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = difficulty,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when (difficulty) {
                            "Khó" -> Color.Red
                            "Trung bình" -> Color(0xFFFFA500)
                            else -> AppPrimaryColor
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoryGameScreenPreview() {
    // Preview với dữ liệu mẫu, không gọi vào ViewModel thực tế
    StoryGameContent(
        gamesList = emptyList(),
        isLoading = false,
        onBackClick = {},
        onGameClick = {}
    )
}
