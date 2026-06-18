@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.ViewModel.AdminViewModel
import com.example.endangeredanimals.ui.EndangeredAnimalsTheme
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ManageContributionScreen(
    navController: NavController,
    contributionId: String,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val contributions by viewModel.contributions.collectAsState()
    val accountsMap by viewModel.accountsMap.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val isActionLoading by viewModel.isActionLoading.collectAsState()

    // Tìm bài đóng góp tương ứng
    val contribution = remember(contributions, contributionId) {
        contributions.find { it.contributionId == contributionId }
    }

    val imageUrl = contribution?.imageUrl ?: ""
    val userName = accountsMap[contribution?.accountId] ?: "Người dùng ẩn danh"
    
    val timeString = remember(contribution?.createdAt) {
        try {
            if (contribution?.createdAt == null) "Vừa xong" else {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                formatter.format(parser.parse(contribution.createdAt)!!)
            }
        } catch (e: Exception) { "Gần đây" }
    }

    var showFullScreenImage by remember { mutableStateOf(false) }

    // Dữ liệu cho phần quản lý (Bottom Section)
    val statusMap = mapOf(
        "pending" to "Chờ duyệt",
        "approved" to "Đã duyệt",
        "rejected" to "Từ chối",
        "discussing" to "Đang thảo luận"
    )
    val reverseStatusMap = statusMap.entries.associate { it.value to it.key }
    
    val statusOptions = statusMap.values.toList()
    var selectedStatusText by remember(contribution?.status) { 
        mutableStateOf(statusMap[contribution?.status] ?: statusOptions[0]) 
    }
    var expandedStatus by remember { mutableStateOf(false) }

    // Dữ liệu loài động vật
    val sortedAnimals = remember(animals) {
        animals.sortedBy { it.nameVn ?: "" }
    }

    var selectedAnimalId by remember(contribution?.finalAnimalId) { 
        mutableStateOf(contribution?.finalAnimalId ?: "") 
    }
    val selectedAnimal = animals.find { it.animalID == selectedAnimalId }
    val selectedAnimalName = if (selectedAnimal != null) {
        "${selectedAnimal.nameVn} (${selectedAnimal.nameLatin})"
    } else "Chưa xác định"
    var expandedAnimal by remember { mutableStateOf(false) }

    // Phân tích AI Prediction JSON
    val aiResultText = remember(contribution?.aiPrediction) {
        try {
            val jsonStr = contribution?.aiPrediction?.toString()
            if (!jsonStr.isNullOrBlank()) {
                val arr = JSONArray(jsonStr)
                if (arr.length() > 0) {
                    val first = arr.getJSONObject(0)
                    val species = first.getString("speciesName")
                    val confidence = first.getInt("confidence")
                    "$species ($confidence%)"
                } else "Không có dự đoán"
            } else "Chưa có kết quả AI"
        } catch (e: Exception) { "Lỗi đọc AI" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết kiểm duyệt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        val finalStatus = reverseStatusMap[selectedStatusText] ?: "pending"
                        viewModel.updateContribution(
                            contributionId = contributionId,
                            newStatus = finalStatus,
                            finalAnimalId = if (selectedAnimalId.isBlank()) null else selectedAnimalId
                        ) { success ->
                            if (success) {
                                navController.popBackStack()
                            } else {
                                android.widget.Toast.makeText(context, "Cập nhật thất bại. Vui lòng kiểm tra quyền Admin!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37ab3c)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isActionLoading && contribution != null
                ) {
                    if (isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Lưu Thay Đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (contribution == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy thông tin bài đóng góp")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Thông tin đóng góp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Ảnh đóng góp",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable { showFullScreenImage = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoAdminRow(label = "Người đóng góp:", value = userName)
                        InfoAdminRow(label = "Thời gian:", value = timeString)
                        InfoAdminRow(label = "Tọa độ GPS:", value = if (contribution.latitude != null) "${contribution.latitude}, ${contribution.longitude}" else "Không có")
                        InfoAdminRow(label = "Ghi chú:", value = contribution.userNote ?: "Không có ghi chú")
                        InfoAdminRow(label = "AI Dự đoán:", value = aiResultText)

                        Spacer(modifier = Modifier.height(16.dp))

                        // NÚT XEM THẢO LUẬN CỘNG ĐỒNG
                        Button(
                            onClick = { navController.navigate("discuss_detail/$contributionId") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8F5E9),
                                contentColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xem thảo luận cộng đồng", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kiểm duyệt & Chốt kết quả",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text("Trạng thái phê duyệt:", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus }
                        ) {
                            OutlinedTextField(
                                value = selectedStatusText,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                statusOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedStatusText = option
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val isApproved = selectedStatusText == "Đã duyệt"

                        Text(
                            text = "Định danh loài chính thức:",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isApproved) Color.Unspecified else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedAnimal && isApproved,
                            onExpandedChange = { if (isApproved) expandedAnimal = !expandedAnimal }
                        ) {
                            OutlinedTextField(
                                value = selectedAnimalName,
                                onValueChange = {},
                                readOnly = true,
                                enabled = isApproved,
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAnimal && isApproved) 
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedAnimal && isApproved,
                                onDismissRequest = { expandedAnimal = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chưa xác định") },
                                    onClick = {
                                        selectedAnimalId = ""
                                        expandedAnimal = false
                                    }
                                )
                                sortedAnimals.forEach { animal ->
                                    DropdownMenuItem(
                                        text = { Text("${animal.nameVn} (${animal.nameLatin})") },
                                        onClick = {
                                            selectedAnimalId = animal.animalID ?: ""
                                            expandedAnimal = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullScreenImage) {
        RotatableZoomableImageDialog(
            imageUrl = imageUrl,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
fun InfoAdminRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.65f)
        )
    }
}

@Composable
fun RotatableZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            val maxOffsetX = (size.width * (scale - 1)) / 2
                            val maxOffsetY = (size.height * (scale - 1)) / 2
                            if (scale > 1f) {
                                offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Ảnh phóng to",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                            rotationZ = rotation
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                }

                IconButton(
                    onClick = { rotation += 90f },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Xoay ảnh", tint = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Màn hình kiểm duyệt")
@Composable
fun ManageContributionScreenPreview() {
    val fakeNavController = rememberNavController()
    EndangeredAnimalsTheme {
        ManageContributionScreen(
            navController = fakeNavController,
            contributionId = "mock_id_001"
        )
    }
}