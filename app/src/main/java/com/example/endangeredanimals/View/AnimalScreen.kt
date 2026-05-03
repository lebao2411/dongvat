@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.endangeredanimals.Component.ZoomableImageDialog
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.AnimalDetailViewModel
import com.example.endangeredanimals.ViewModel.FavoriteViewModel
import com.example.endangeredanimals.ui.Neutral100

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnimalScreen(
    navController: NavController,
    animalId: String,
    animalDetailViewModel: AnimalDetailViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel()
) {
    val animal by animalDetailViewModel.animal.collectAsState()
    val favoriteAnimals by favoriteViewModel.favoriteAnimals.collectAsState()
    val isRefreshing by animalDetailViewModel.isRefreshing.collectAsState()
    val isLoading by animalDetailViewModel.isLoading.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()
    var showFullScreenImage by remember { mutableStateOf(false) }

    // XỬ LÝ OPTIMISTIC UI: Trạng thái thật từ DB
    val isFavoriteTrueState = favoriteAnimals.any { it.animalID == animalId }

    // Trạng thái hiển thị tức thì trên UI
    var optimisticFavorite by remember(isFavoriteTrueState) { mutableStateOf(isFavoriteTrueState) }

    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = animalId) {
        animalDetailViewModel.loadAnimalDetails(animalId)
        favoriteViewModel.loadFavoriteAnimals()
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
            containerColor = Neutral100
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { animalDetailViewModel.refresh(animalId) },
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = Color.White,
                    color = Color(0xFF37ab3c),
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (isLoading && animal == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF37ab3c))
                }
            } else {
                animal?.let { loadedAnimal ->
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                        // KHỐI CHỨA ẢNH ĐÃ ĐƯỢC CẬP NHẬT UI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp, start = 16.dp, end = 16.dp)
                                .animateContentSize(animationSpec = tween(durationMillis = 300))
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!loadedAnimal.imageUrl.isNullOrBlank()) {
                                                showFullScreenImage = true
                                            }
                                        }
                                )
                            }

                            // NÚT THOÁT CHUỒNG (Vẫn đè lên góc Card)
                            Surface(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 12.dp)
                                    .size(40.dp)
                                    .align(Alignment.TopStart),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Thoát",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                optimisticFavorite = !optimisticFavorite

                                favoriteViewModel.toggleFavorite(
                                    animalId = animalId,
                                    isCurrentlyFavorite = isFavoriteTrueState,
                                    onComplete = {
                                        favoriteViewModel.loadFavoriteAnimals()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                // Sử dụng biến optimistic để hiển thị màu
                                containerColor = if (optimisticFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (optimisticFavorite) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (optimisticFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = if (optimisticFavorite) "Đã yêu thích" else "Yêu thích",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // THÔNG TIN CƠ BẢN
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                        // THÔNG TIN CHI TIẾT
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 20.dp)) {
                            Text(
                                text = "Thông Tin Chi Tiết",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            InfoRow("Tên khác", loadedAnimal.otherNames ?: "Chưa có thông tin")
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
                        Text("Không tìm thấy thông tin động vật.")
                    }
                }
            }
        }
    }

    if (showFullScreenImage && !animal?.imageUrl.isNullOrBlank()) {
        ZoomableImageDialog(
            imageUrl = animal!!.imageUrl!!,
            onDismiss = { showFullScreenImage = false }
        )
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