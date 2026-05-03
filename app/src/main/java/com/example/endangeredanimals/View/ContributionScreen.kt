package com.example.endangeredanimals.View

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class ContributionImage(
    val uri: Uri,
    val isValid: Boolean = true // Tạm thời để true, sau này ViewModel sẽ check EXIF và update cái này
)

@Composable
fun ContributeScreen(
    navController: NavController,
    initialImageUri: Uri?,
    aiSpeciesResult: String?
) {
    val context = LocalContext.current

    // Danh sách các ảnh người dùng muốn đóng góp
    var selectedImages by remember {
        mutableStateOf(initialImageUri?.let { listOf(ContributionImage(it)) } ?: emptyList())
    }

    // STATE MỚI: Lưu trữ mô tả của người dùng
    var description by remember { mutableStateOf("") }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = uris.map { ContributionImage(it) }
            selectedImages = selectedImages + newImages
        }
    }

    val hasInvalidImage = selectedImages.any { !it.isValid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .imePadding() // RẤT QUAN TRỌNG: Tự động đẩy nội dung lên khi bàn phím ảo xuất hiện
    ) {
        Text(
            text = "Đóng góp hình ảnh",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (aiSpeciesResult != null) {
            Text(
                text = "Loài phát hiện: $aiSpeciesResult",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // BẢNG LƯỚI ẢNH (Image Grid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Lưới ảnh sẽ co giãn chiếm phần không gian còn lại
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AddMoreButton {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }

            items(selectedImages) { imageItem ->
                ImageGridItem(
                    image = imageItem,
                    onRemove = {
                        selectedImages = selectedImages.filter { it.uri != imageItem.uri }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // KHUNG NHẬP MÔ TẢ (TEXTFIELD)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp), // Đặt chiều cao tối thiểu để giống một khung viết truyện
            label = { Text("Thông tin bổ sung (Tùy chọn)") },
            placeholder = {
                Text(
                    text = "Nhập địa điểm, thời gian hoặc câu chuyện về khoảnh khắc bạn chụp được...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            maxLines = 5 // Cho phép gõ tối đa 5 dòng rồi mới cuộn chữ
        )

        Spacer(modifier = Modifier.height(16.dp))

        // KHUNG CẢNH BÁO (Chỉ hiện khi có ảnh lỗi EXIF)
        if (hasInvalidImage) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Cảnh báo", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phát hiện ảnh không hợp lệ (ảnh mạng). Vui lòng xóa ảnh bị đánh dấu đỏ.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // NÚT GỬI
        Button(
            // Lúc này nút gửi sẽ lấy được cả danh sách ảnh (selectedImages) và chữ (description)
            onClick = { /* Gọi ViewModel upload ảnh và gửi kèm biến description */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            enabled = selectedImages.isNotEmpty() && !hasInvalidImage
        ) {
            Text("Gửi lên cộng đồng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Component cho ô "Thêm ảnh"
@Composable
fun AddMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = "Thêm", tint = MaterialTheme.colorScheme.primary)
            Text("Thêm ảnh", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Component cho từng bức ảnh trong Lưới
@Composable
fun ImageGridItem(image: ContributionImage, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.uri)
                .crossfade(true)
                .build(),
            contentDescription = "Ảnh đã chọn",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!image.isValid) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.3f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.White, modifier = Modifier.size(16.dp))
        }

        Icon(
            imageVector = if (image.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = "Trạng thái",
            tint = if (image.isValid) Color(0xFF4CAF50) else Color.Red,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewContributionScreen() {
    val fakeNavController = rememberNavController()
    ContributeScreen(navController = fakeNavController, initialImageUri = null, aiSpeciesResult = null)
}