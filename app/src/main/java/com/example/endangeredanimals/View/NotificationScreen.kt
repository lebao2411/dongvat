@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.endangeredanimals.Model.Notification
import com.example.endangeredanimals.ViewModel.NotificationViewModel
import com.example.endangeredanimals.ui.Green500
import com.example.endangeredanimals.ui.Green50
import com.example.endangeredanimals.ui.Green700
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                actions = {
                    if (notifications.any { it.isRead == false }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Đọc tất cả", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFFAFAFA)),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = Color.White,
                    color = Green500,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (isLoading && notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green500)
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bạn chưa có thông báo nào.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onClick = {
                                viewModel.markAsRead(notif.notificationId)

                                // Điều hướng thẳng đến trang DiscussScreen nhưng mang theo ID
                                if (!notif.referenceId.isNullOrBlank()) {
                                    when (notif.type) {
                                        "COMMENT", "CONTRIBUTION_DISCUSSING" -> {
                                            navController.navigate("discuss_detail/${notif.referenceId}")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(notification: Notification, onClick: () -> Unit) {
    val isUnread = notification.isRead == false
    val backgroundColor = if (isUnread) Green50 else Color.White

    val iconData = when (notification.type) {
        "LIKE" -> Pair(Icons.Default.Favorite, Color(0xFFE91E63))
        "COMMENT" -> Pair(Icons.Default.Comment, Color(0xFF2196F3))
        "RANK" -> Pair(Icons.Default.EmojiEvents, Color(0xFFFFC107))
        "CONTRIBUTION_APPROVED" -> Pair(Icons.Default.CheckCircle, Green500)
        "CONTRIBUTION_REJECTED" -> Pair(Icons.Default.Cancel, Color.Red)
        else -> Pair(Icons.Default.Notifications, Color.Gray)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconData.second.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconData.first, contentDescription = null, tint = iconData.second, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title ?: "",
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (isUnread) Color.Black else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                notification.createdAt?.let { dateString ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatTime(dateString),
                        fontSize = 12.sp,
                        color = Green700
                    )
                }
            }

            if (isUnread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Red))
            }
        }
    }
}

fun formatTime(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        if (date != null) formatter.format(date) else isoString
    } catch (e: Exception) {
        isoString
    }
}