@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.endangeredanimals.ui.AppBackgroundCard
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
    var showStatusDialog by remember { mutableStateOf(false) }

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

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Chú Giải Tình Trạng Bảo Tồn") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.status_animal),
                        contentDescription = "Bảng chú giải các cấp độ bảo tồn",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showStatusDialog = false }) {
                    Text("Đóng")
                }
            },
            containerColor = AppBackgroundCard
        )
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
                        placeholder = painterResource(R.drawable.loading),
                        error = painterResource(R.drawable.noimage),
                        contentDescription = loadedAnimal.nameVn,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Thoát",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
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
                        modifier = Modifier.weight(1.5f),
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

                    Row(
                        modifier = Modifier.clickable { showStatusDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tình trạng: ${loadedAnimal.status ?: "Không xác định"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Xem chú giải",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông Tin Chi Tiết",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoRow("Lớp", loadedAnimal.animalGroup ?: "Chưa có thông tin")
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
