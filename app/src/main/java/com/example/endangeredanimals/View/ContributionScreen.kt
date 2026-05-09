package com.example.endangeredanimals.View

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.ViewModel.ContributionViewModel

// Đảm bảo Data Class này khớp với những gì ViewModel đang dùng
data class ContributionImage(
    val uri: Uri,
    val isValid: Boolean = true,
    val isLoading: Boolean = false, // Trạng thái đang quét AI/EXIF
    val errorMessage: String? = null
)

@Composable
fun ContributeScreen(
    navController: NavController,
    initialImageUri: Uri?,
    aiSpeciesResult: String?,
    viewModel: ContributionViewModel = viewModel() // KẾT NỐI VỚI VIEWMODEL
) {
    val context = LocalContext.current

    // Lắng nghe danh sách ảnh từ ViewModel thay vì tạo biến cục bộ
    val selectedImages by viewModel.images.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var description by remember {
        mutableStateOf(if (!aiSpeciesResult.isNullOrBlank()) "$aiSpeciesResult\n\n" else "")
    }

    // Tự động ném ảnh từ Scanner vào ViewModel để quét
    LaunchedEffect(initialImageUri) {
        if (initialImageUri != null && selectedImages.isEmpty()) {
            viewModel.addImages(context, listOf(initialImageUri))
        }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            // Ném ảnh mới chọn vào ViewModel để quét EXIF/AI
            viewModel.addImages(context, uris)
        }
    }

    val hasInvalidImage = selectedImages.any { !it.isValid }
    val hasLoadingImage = selectedImages.any { it.isLoading }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
            .imePadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Đóng góp hình ảnh",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().weight(1f),
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
                    onRemove = { viewModel.removeImage(imageItem.uri) } // Xóa ảnh qua ViewModel
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            label = { Text("Thông tin bổ sung (Tùy chọn)") },
            placeholder = { Text("Nhập địa điểm, thời gian hoặc câu chuyện...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hiển thị lỗi cụ thể từ ViewModel trả về
        if (hasInvalidImage) {
            val errorMsg = selectedImages.firstOrNull { !it.isValid }?.errorMessage ?: "Phát hiện ảnh không hợp lệ."
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = "Cảnh báo", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = {
                viewModel.uploadContributions(context, description, aiSpeciesResult) {
                    // 1. GỌI TOAST THÔNG BÁO THÀNH CÔNG Ở ĐÂY
                    Toast.makeText(
                        context,
                        "Gửi đóng góp thành công! Cảm ơn bạn.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()

                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            enabled = selectedImages.isNotEmpty() && !hasInvalidImage && !hasLoadingImage && !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Gửi lên cộng đồng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = "Thêm", tint = MaterialTheme.colorScheme.primary)
            Text("Thêm ảnh", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ImageGridItem(image: ContributionImage, onRemove: () -> Unit) {
    Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(image.uri).crossfade(true).build(),
            contentDescription = "Ảnh",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!image.isValid) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.3f)))
        }

        // Hiện vòng xoay mờ khi đang quét EXIF/AI
        if (image.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            }
        } else {
            // Nút xóa ảnh ở góc trên
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // ICON TRẠNG THÁI (GÓC DƯỚI TRÁI)
            if (image.isValid) {
                // Dấu Tick xanh
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Hợp lệ",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(20.dp)
                )
            } else {
                // Hình tròn đỏ với dấu X màu trắng như bạn yêu cầu
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Lỗi",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp) // Kích thước chữ X nhỏ lại để vừa lọt lòng hình tròn
                    )
                }
            }
        }
    }
}