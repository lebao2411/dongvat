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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ui.Neutral100
import com.example.endangeredanimals.ui.Neutral200
import com.example.endangeredanimals.ui.Neutral50

@Composable
fun MenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToContribution: () -> Unit,
    onNavigateToDiscuss: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // --- NHÓM TÀI KHOẢN ---
        MenuSectionTitle(title = "Tài khoản")
        MenuItemCard(
            icon = Icons.Default.Person,
            title = "Hồ sơ cá nhân",
            subtitle = "Quản lý thông tin và điểm số",
            onClick = onNavigateToProfile // Gọi callback
        )

        // --- NHÓM CỘNG ĐỒNG ---
        MenuSectionTitle(title = "Cộng đồng bảo tồn")
        MenuItemCard(
            iconPainter = painterResource(R.drawable.animal),
            title = "Đóng góp ảnh",
            onClick = onNavigateToContribution // Gọi callback
        )
        Spacer(modifier = Modifier.height(7.dp))

        // ĐÃ THÊM ĐIỀU HƯỚNG TỚI THẢO LUẬN
        MenuItemCard(
            iconPainter = painterResource(R.drawable.discuss),
            title = "Thảo luận cộng đồng",
            onClick = onNavigateToDiscuss // Gọi callback
        )
        Spacer(modifier = Modifier.height(7.dp))

        MenuItemCard(
            iconPainter = painterResource(R.drawable.leader_board),
            title = "Bảng xếp hạng",
            onClick = { /* TODO: Thêm onNavigateToLeaderboard sau */ }
        )

        // --- NHÓM HỆ THỐNG ---
        MenuSectionTitle(title = "Hệ thống")
        MenuItemCard(
            icon = Icons.Default.Settings,
            title = "Cài đặt ứng dụng",
            onClick = { /* TODO: Thêm onNavigateToSettings sau */ }
        )
        Spacer(modifier = Modifier.height(7.dp))

        MenuItemCard(
            icon = Icons.Default.ExitToApp,
            title = "Đăng xuất",
            iconTint = Color.Red,
            textColor = Color.Red,
            onClick = onLogout // Gọi callback
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

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

@Composable
fun MenuItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Đi tiếp", tint = Color.LightGray)
        }
    }
}

@Composable
fun MenuItemCard(
    iconPainter: Painter,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = iconPainter, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Đi tiếp", tint = Color.LightGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuScreen() {
    // Không cần FakeNavController rườm rà nữa, chỉ cần truyền lambda rỗng
    MenuScreen(
        onNavigateToProfile = {},
        onNavigateToContribution = {},
        onNavigateToDiscuss = {},
        onLogout = {}
    )
}