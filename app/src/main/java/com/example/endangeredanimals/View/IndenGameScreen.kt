package com.example.endangeredanimals.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.ViewModel.IndenGameViewModel
import com.example.endangeredanimals.ViewModel.IndenGameState
import com.example.endangeredanimals.ViewModel.IndenQuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndenGameScreen(
    navController: NavController,
    gameViewModel: IndenGameViewModel = viewModel()
) {
    val gameState by gameViewModel.gameState.collectAsState()

    var showResultDialog by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf<IndenQuizQuestion?>(null) }

    if (gameState is IndenGameState.Success) {
        currentQuestion = (gameState as IndenGameState.Success).question
    }

    if (showResultDialog && currentQuestion != null) {
        ResultDialog(
            correctAnswer = currentQuestion!!.correctAnimal.nameVn ?: "Không rõ",
            onContinue = {
                showResultDialog = false
                gameViewModel.generateNewQuestion() // Yêu cầu ViewModel tạo câu hỏi mới
            },
            onExit = {
                showResultDialog = false
                navController.popBackStack() // Thoát về màn hình trước
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đố Vui Động Vật") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Dựa vào trạng thái game để quyết định hiển thị gì
            when (val state = gameState) {
                is IndenGameState.Loading -> {
                    // Hiển thị vòng quay loading khi đang tải
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
                }
                is IndenGameState.Success -> {
                    // Hiển thị nội dung chính của game
                    GameContent(
                        question = state.question,
                        onAnswerSelected = {
                            // Khi người dùng chọn 1 đáp án, chỉ cần hiển thị dialog
                            showResultDialog = true
                        }
                    )
                }
                is IndenGameState.Error -> {
                    // Hiển thị thông báo lỗi
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            "Đã xảy ra lỗi hoặc không có đủ dữ liệu để chơi. Vui lòng thử lại sau.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameContent(question: IndenQuizQuestion, onAnswerSelected: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Ảnh và gợi ý
        item {
            Text("Đây là con gì?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(question.correctAnimal.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Câu hỏi về động vật",
                    modifier = Modifier.fillMaxSize(), // Ảnh sẽ lấp đầy Card
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Gợi ý: Loài này thường được tìm thấy ở ${question.correctAnimal.location ?: "nhiều nơi"}.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
        }

        // Hiển thị 4 lựa chọn đáp án
        items(question.options.size) { index ->
            val answerText = question.options[index]
            AnswerCard(text = answerText, onClick = { onAnswerSelected(answerText) })
            if (index < question.options.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


// Composable cho mỗi thẻ đáp án
@Composable
private fun AnswerCard(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

// Composable cho Dialog hiển thị kết quả
@Composable
private fun ResultDialog(
    correctAnswer: String,
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Không làm gì khi bấm ra ngoài */ },
        title = { Text("Kết quả", fontWeight = FontWeight.Bold) },
        text = { Text("Đáp án đúng là: $correctAnswer") },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Tiếp tục")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onExit) {
                Text("Thoát")
            }
        }
    )
}
