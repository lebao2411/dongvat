package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.ViewModel.AdminViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    rootController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val contributions by viewModel.contributions.collectAsState()
    val accountsMap by viewModel.accountsMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State quản lý việc mở/đóng DropdownMenu
    var isFilterExpanded by remember { mutableStateOf(false) }

    // State quản lý tiêu chí lọc (true = Mới nhất, false = Cũ nhất)
    var isNewestFirst by remember { mutableStateOf(true) }

    // Xử lý logic sắp xếp danh sách dựa trên State
    val sortedContributions = remember(isNewestFirst, contributions) {
        if (isNewestFirst) {
            contributions.sortedByDescending { it.createdAt }
        } else {
            contributions.sortedBy { it.createdAt }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đóng góp", fontWeight = FontWeight.Bold) },
                actions = {
                    // Nút đăng xuất bên góc phải
                    TextButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val gso = GoogleSignInOptions.Builder(
                                        GoogleSignInOptions.DEFAULT_SIGN_IN
                                    ).build()
                                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                    googleSignInClient.signOut()
                                    com.example.endangeredanimals.Component.SupabaseInstance.client.auth.signOut()

                                    withContext(Dispatchers.Main) {
                                        rootController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Đăng xuất",
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White // Set nền tổng thể màu trắng
    ) { paddingValues ->
        if (isLoading && contributions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                // 2. Khu vực Nút Lọc (Filter)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    OutlinedButton(onClick = { isFilterExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Icon Lọc",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (isNewestFirst) "Mới nhất" else "Cũ nhất")
                    }

                    DropdownMenu(
                        expanded = isFilterExpanded,
                        onDismissRequest = { isFilterExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Bài đóng góp mới nhất") },
                            onClick = {
                                isNewestFirst = true
                                isFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bài đóng góp cũ nhất") },
                            onClick = {
                                isNewestFirst = false
                                isFilterExpanded = false
                            }
                        )
                    }
                }

                // 3. Danh sách (LazyColumn)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách giữa các Card
                ) {
                    items(sortedContributions) { item ->
                        val userName = accountsMap[item.accountId] ?: "Người dùng ẩn danh"
                        ContributionCard(
                            item = item,
                            userName = userName,
                            onClick = {
                                rootController.navigate("manage_contribution/${item.contributionId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// 4. Component hiển thị từng bài đóng góp
@Composable
private fun ContributionCard(item: Contribution, userName: String, onClick: () -> Unit) {
    val timeString = remember(item.createdAt) {
        try {
            if (item.createdAt == null) "Vừa xong" else {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                formatter.format(parser.parse(item.createdAt)!!)
            }
        } catch (e: Exception) { "Gần đây" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        // Tạo hiệu ứng đổ bóng để Card nổi lên so với nền trắng
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh đóng góp
            AsyncImage(
                model = item.imageUrl,
                contentDescription = "Ảnh đóng góp của $userName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray) // Màu nền placeholder khi đang tải ảnh
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Thông tin Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeString,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                // Hiển thị trạng thái
                val statusColor = when(item.status) {
                    "approved" -> Color(0xFF37ab3c)
                    "pending" -> Color(0xFFFFA000)
                    "rejected" -> Color.Red
                    "discussing" -> Color.Blue
                    else -> Color.Gray
                }
                
                val statusText = when(item.status) {
                    "approved" -> "Đã duyệt"
                    "pending" -> "Chờ duyệt"
                    "rejected" -> "Từ chối"
                    "discussing" -> "Đang thảo luận"
                    else -> "Chưa rõ"
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewManageScreem(){
    val fakeNavController = rememberNavController()
    AdminManagementScreen(rootController = fakeNavController)
}