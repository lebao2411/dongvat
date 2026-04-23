package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun MenuScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        )

        // --- NHÓM TÀI KHOẢN ---
        MenuSectionTitle(title = "Tài khoản")
        MenuItemCard(
            icon = Icons.Default.Person,
            title = "Hồ sơ cá nhân",
            subtitle = "Quản lý thông tin và điểm số",
            onClick = { navController.navigate("profile") }
        )

        // --- NHÓM CỘNG ĐỒNG ---
        MenuSectionTitle(title = "Cộng đồng bảo tồn")
        MenuItemCard(
            icon = Icons.Default.Favorite,
            title = "Đóng góp của tôi",
            onClick = { /* Điều hướng đến trang đóng góp */ }
        )
        Spacer(modifier = Modifier.height(7.dp)) // Khoảng cách 7.dp theo yêu cầu

        MenuItemCard(
            icon = Icons.AutoMirrored.Filled.List,
            title = "Thảo luận cộng đồng",
            onClick = { /* Điều hướng đến trang thảo luận */ }
        )
        Spacer(modifier = Modifier.height(7.dp))

        MenuItemCard(
            icon = Icons.Default.Star,
            title = "Bảng xếp hạng",
            onClick = { /* Điều hướng đến trang BXH */ }
        )

        // --- NHÓM HỆ THỐNG ---
        MenuSectionTitle(title = "Hệ thống")
        MenuItemCard(
            icon = Icons.Default.Settings,
            title = "Cài đặt ứng dụng",
            onClick = { /* Điều hướng đến trang cài đặt */ }
        )
        Spacer(modifier = Modifier.height(7.dp))

        MenuItemCard(
            icon = Icons.Default.ExitToApp,
            title = "Đăng xuất",
            iconTint = Color.Red,
            textColor = Color.Red,
            onClick = {
                // Gọi hàm đăng xuất của Supabase tại đây
                // Ví dụ: viewModel.logOut()
                navController.navigate("login") {
                    popUpTo(0) // Xóa toàn bộ lịch sử màn hình để không back lại được
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp)) // Đệm dưới cùng để không bị che bởi BottomBar
    }
}

// Component hiển thị Tiêu đề cho từng nhóm (Tài khoản, Cộng đồng...)
@Composable
fun MenuSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp)
    )
}

// Component Cốt lõi: Tạo ra một Card nhỏ nổi lên cho từng lựa chọn
@Composable
fun MenuItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Color(0xFF4CAF50), // Màu xanh lá mặc định hợp với app bảo tồn
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Biến toàn bộ Card thành nút bấm
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Tạo độ nổi
        shape = RoundedCornerShape(12.dp) // Bo góc tròn trịa
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Khối chứa Icon (Tạo nền mờ cùng màu với Icon để nhìn xịn xò hơn)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Khối chứa Text (Title & Subtitle)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                // Nếu truyền subtitle vào thì mới hiển thị
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Mũi tên chỉ hướng bên phải
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Đi tiếp",
                tint = Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuScreen( ) {
    val fakeNavController = rememberNavController()

    MenuScreen(navController = fakeNavController)
}