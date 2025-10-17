@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R

// Lớp dữ liệu mẫu cho chi tiết động vật
data class AnimalDetails(
    val id: Int,
    val vietnameseName: String,
    val scientificName: String,
    val conservationStatus: String,
    val imageRes: Int,
    val animalClass: String, // Lớp
    val order: String, // Bộ
    val distribution: String, // Phân bố
    val populationStatus: String, // Hiện trạng quần thể
    val populationTrend: String, // Xu hướng quần thể
    val habitatType: String, // Dạng sinh cảnh sống
    val habitatDistribution: String, // Dạng sinh cảnh phân bố
    val reproduction: String, // Sinh sản
    val food: String, // Thức ăn
    val threats: String // Mối đe dọa
)

// Dữ liệu mẫu cho một con vật cụ thể
private val sampleAnimalDetail = AnimalDetails(
    id = 1,
    vietnameseName = "Hổ Amur",
    scientificName = "Panthera tigris altaica",
    conservationStatus = "Nguy cấp (EN)",
    imageRes = R.drawable.avata, // Thay bằng ID ảnh của bạn
    animalClass = "Thú (Mammalia)",
    order = "Ăn thịt (Carnivora)",
    distribution = "Vùng Viễn Đông Nga và một phần nhỏ của Trung Quốc",
    populationStatus = "Khoảng 540 cá thể hoang dã",
    populationTrend = "Ổn định",
    habitatType = "Rừng Taiga, rừng hỗn hợp",
    habitatDistribution = "Phân mảnh",
    reproduction = "Mang thai khoảng 103 ngày, đẻ 2-4 con",
    food = "Hươu, lợn rừng, nai sừng tấm",
    threats = "Săn trộm, mất môi trường sống"
)

@Composable
fun AnimalScreen(navController: NavController) {
    // Trạng thái yêu thích
    var isFavorite by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // TopAppBar tùy chỉnh của màn hình này
            AnimalDetailTopAppBar(
                isFavorite = isFavorite,
                onFavoriteClick = { isFavorite = !isFavorite },
                onBackClick = {
                    // Quay lại màn hình trước đó
                    navController.popBackStack()
                }
            )
        },
        // Scaffold sẽ tự động xử lý phần đệm cho nội dung bên dưới TopAppBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Áp dụng padding từ Scaffold
                .verticalScroll(rememberScrollState()) // Cho phép cuộn toàn màn hình
        ) {
            // Nội dung chi tiết của con vật
            AnimalContent(animal = sampleAnimalDetail)
        }
    }
}

@Composable
private fun AnimalDetailTopAppBar(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { /* Để trống vì không có tiêu đề */ },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        actions = {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Yêu thích",
                    tint = if (isFavorite) Color.Red else LocalContentColor.current
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            // Làm cho TopAppBar trong suốt để thấy nội dung bên dưới khi cuộn
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    )
}

/**
 * Phần nội dung chính, bao gồm ảnh và các bảng thông tin.
 */
@Composable
private fun AnimalContent(animal: AnimalDetails) {
    Column {
        // 1. Card ảnh và thông tin cơ bản
        Image(
            painter = painterResource(id = animal.imageRes),
            contentDescription = animal.vietnameseName,
            contentScale = ContentScale.Crop, // Lấp đầy không gian mà không méo ảnh
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Tạo ảnh hình vuông
        )

        // Phần thông tin ngay dưới ảnh
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(animal.vietnameseName, style = MaterialTheme.typography.headlineMedium)
            Text(
                animal.scientificName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tình trạng: ${animal.conservationStatus}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error // Dùng màu đỏ cho tình trạng nguy cấp
            )
        }

        // Đường kẻ phân cách
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // 2. Bảng thông tin chi tiết
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow("Lớp", animal.animalClass)
            InfoRow("Bộ", animal.order)
            InfoRow("Phân bố", animal.distribution)
            InfoRow("Hiện trạng quần thể", animal.populationStatus)
            InfoRow("Xu hướng quần thể", animal.populationTrend)
            InfoRow("Dạng sinh cảnh sống", animal.habitatType)
            InfoRow("Dạng sinh cảnh phân bố", animal.habitatDistribution)
            InfoRow("Sinh sản", animal.reproduction)
            InfoRow("Thức ăn", animal.food)
            InfoRow("Mối đe dọa", animal.threats)
        }
    }
}

/**
 * Một hàng trong bảng thông tin, gồm Tiêu đề và Nội dung.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Cột Tiêu đề
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f) // Tiêu đề chiếm 40% chiều rộng
        )
        // Cột Nội dung
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f) // Nội dung chiếm 60% chiều rộng
        )
    }
}


@Preview(showBackground = true, name = "Animal Screen Preview")
@Composable
fun AnimalScreenPreview() {
    MaterialTheme {
        // Để xem trước, chúng ta tạo một NavController giả
        AnimalScreen(navController = rememberNavController())
    }
}
