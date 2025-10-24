@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.AnimalDetailViewModel
import com.example.endangeredanimals.ViewModel.FavoriteViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnimalScreen(
    navController: NavController,
    animalId: String,
    animalDetailViewModel: AnimalDetailViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel()
) {
    val systemUiController = rememberSystemUiController()

    val animal by animalDetailViewModel.animal.collectAsState()
    val favoriteAnimals by favoriteViewModel.favoriteAnimals.collectAsState()

    val isFavorite = favoriteAnimals.any { it.animalID == animalId }

    LaunchedEffect(key1 = animalId) {
        animalDetailViewModel.loadAnimalDetails(animalId)
        favoriteViewModel.loadFavoriteAnimals()
        systemUiController.isSystemBarsVisible = false
    }

    DisposableEffect(Unit) {
        onDispose {
            systemUiController.isSystemBarsVisible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        animal?.let { loadedAnimal ->
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box {
                    val imageModel = if (loadedAnimal.imageUrl.isNullOrBlank()) {
                        R.drawable.protect_animals
                    } else {
                        loadedAnimal.imageUrl
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.avata),
                        error = painterResource(R.drawable.save_animal),
                        contentDescription = loadedAnimal.nameVn,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / 1f)
                    )
                }

                Button(
                    onClick = {
                        favoriteViewModel.toggleFavorite(
                            animalId = animalId,
                            isCurrentlyFavorite = isFavorite,
                            onComplete = {
                                favoriteViewModel.loadFavoriteAnimals()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isFavorite) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = if (isFavorite) "Đã yêu thích" else "Yêu thích",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = loadedAnimal.nameVn ?: "Chưa có tên tiếng Việt",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = loadedAnimal.nameLatin ?: "Chưa có tên khoa học",
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tình trạng: ${loadedAnimal.status ?: "Không xác định"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông Tin Chi Tiết",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoRow("Lớp", loadedAnimal.animalClass ?: "Chưa có thông tin")
                    InfoRow("Loài (Bộ)", loadedAnimal.species ?: "Chưa có thông tin")
                    InfoRow("Phân bố", loadedAnimal.location ?: "Chưa có thông tin")
                    InfoRow("Hiện trạng quần thể", loadedAnimal.popStatus ?: "Chưa có thông tin")
                    InfoRow("Xu hướng quần thể", loadedAnimal.popTrend ?: "Chưa có thông tin")
                    InfoRow("Đặc điểm sinh cảnh", loadedAnimal.habitatFeat ?: "Chưa có thông tin")
                    InfoRow("Loại sinh cảnh", loadedAnimal.habitatType ?: "Chưa có thông tin")
                    InfoRow("Sinh sản", loadedAnimal.reproduction ?: "Chưa có thông tin")
                    InfoRow("Thức ăn", loadedAnimal.diet ?: "Chưa có thông tin")
                    InfoRow("Mối đe dọa", loadedAnimal.threats ?: "Chưa có thông tin")
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 22.sp,
            modifier = Modifier.weight(0.6f)
        )
    }
}
