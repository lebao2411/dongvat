package com.example.endangeredanimals.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.endangeredanimals.Model.Account
import com.example.endangeredanimals.ViewModel.LeaderboardViewModel
import com.example.endangeredanimals.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val topUsers by viewModel.topUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bảng xếp hạng", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Green500 // Màu chủ đạo của App
                )
            )
        }
    ) { paddingValues ->
        // HIỆU ỨNG BRUSH NỀN TỪ TRÊN XUỐNG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Green500, Neutral50)
                    )
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Green700)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(topUsers) { index, user ->
                        RankItemCard(rank = index + 1, user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun RankItemCard(rank: Int, user: Account) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // VIỀN MÀU GREEN700 NHƯ YÊU CẦU
        border = BorderStroke(1.5.dp, Green700),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị thứ hạng (Top 1, 2, 3 có màu đặc biệt)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = when(rank) {
                            1 -> Color(0xFFFFD700) // Vàng
                            2 -> Color(0xFFC0C0C0) // Bạc
                            3 -> Color(0xFFCD7F32) // Đồng
                            else -> Green100
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.White else Green900
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Ảnh đại diện giả (Hoặc icon Person)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Green50),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Green700, modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(user.title, fontSize = 12.sp, color = Green700)
            }

            // Số điểm đóng góp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${user.score} pts",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Green900
                )
            }
        }
    }
}