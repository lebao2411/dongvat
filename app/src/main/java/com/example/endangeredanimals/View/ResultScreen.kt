package com.example.endangeredanimals.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ResultViewModel


@Composable
fun ResultScreen(
    navController: NavController,
    category: String,
    resultViewModel: ResultViewModel = viewModel()
) {
    // Lấy dữ liệu khi màn hình được tạo lần đầu
    LaunchedEffect(key1 = category) {
        resultViewModel.fetchAnimalsByCategory(category)
    }

    val results by resultViewModel.results.collectAsState()
    val isLoading by resultViewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Có thể thêm AppBar ở đây
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy kết quả nào cho '$category'")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(results, key = { it.animalID }) { animal ->
                    ResultCard(
                        animal = animal,
                        onCardClick = {
                            navController.navigate("animal_screen/${animal.animalID}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(animal: Animal, onCardClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = animal.imageUrl,
                contentDescription = animal.nameVn,
                placeholder = painterResource(id = R.drawable.avata_panda),
                error = painterResource(id = R.drawable.avata_panda),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = animal.nameVn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = animal.nameLatin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "Tình trạng: ${animal.status}", style = MaterialTheme.typography.bodyMedium)
            }

            IconButton(
                onClick = { isFavorite = !isFavorite },
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Yêu thích",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}