package com.example.endangeredanimals.View

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ScannerViewModel

@Composable
fun ScannerScreen(
    navController: NavHostController,
    viewModel: ScannerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable(enabled = uiState.imageUri == null) { // Chỉ cho bấm nếu chưa có ảnh
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh động vật",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // THÊM: NÚT "X" Ở GÓC TRÊN BÊN PHẢI
                    IconButton(
                        onClick = { viewModel.clearImage() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa ảnh",
                            tint = Color.White
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_image),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nhấn để chọn ảnh từ thư viện",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.analyzeImage(context) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            enabled = uiState.imageUri != null && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Bắt đầu nhận diện AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.aiResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Kết quả dự đoán:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.aiResult!!,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // THÊM: LOGIC ĐỂ QUYẾT ĐỊNH XEM CÓ HIỂN THỊ 2 NÚT KHÔNG
            // Dựa vào các câu cảnh báo cứng mà ta đã bắt AI nói trong Prompt
            val isNotAnimal = uiState.aiResult!!.contains("Đây không phải là hình ảnh động vật", ignoreCase = true)
            val isNotLocal = uiState.aiResult!!.contains("không phải động vật bản địa", ignoreCase = true)

            // Chỉ hiển thị 2 nút chức năng nếu ĐÚNG là động vật VÀ CÓ ở Việt Nam
            if (!isNotAnimal && !isNotLocal) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            uiState.matchedAnimalId?.let { id ->
                                navController.navigate("animal_screen/$id")
                            }
                        },
                        // Nút bị mờ đi nếu đang tìm DB hoặc tìm không ra ID
                        enabled = uiState.matchedAnimalId != null && !uiState.isSearchingDb,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        if (uiState.isSearchingDb) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (uiState.matchedAnimalId == null) {
                            Text("Chưa có Dữ liệu", fontSize = 12.sp) // Cảnh báo nhẹ nếu DB chưa có
                        } else {
                            Text("Thông tin loài", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Button(
                        onClick = {
                            val lines = uiState.aiResult!!.lines()

                            val filteredInfo = lines.filter {
                                it.startsWith("Tên loài:") || it.startsWith("Độ tự tin:")
                            }.joinToString("\n")

                            val encodedUri = Uri.encode(uiState.imageUri.toString())
                            val aiResultEncoded = Uri.encode(filteredInfo)
                            navController.navigate("contribute_screen/$encodedUri/$aiResultEncoded")
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Đóng góp ảnh")
                    }
                }
            }
        }
    }
}