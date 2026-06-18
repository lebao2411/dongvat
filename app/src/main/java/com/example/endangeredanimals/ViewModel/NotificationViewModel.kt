package com.example.endangeredanimals.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.Notification
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val client = SupabaseInstance.client

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Biến đếm số thông báo chưa đọc để hiển thị chấm đỏ trên Icon Chuông
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        fetchNotifications()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchNotifications()
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val user = client.auth.currentSessionOrNull()?.user
            if (user == null) {
                _isLoading.value = false
                return@launch
            }

            try {
                val result = client.from("notifications")
                    .select {
                        filter { eq("accountId", user.id) }
                        order("createdAt", order = Order.DESCENDING)
                        limit(20) // Chỉ lấy 20 thông báo mới nhất cho nhẹ App
                    }
                    .decodeList<Notification>()

                _notifications.value = result
                _unreadCount.value = result.count { it.isRead == false }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi tải thông báo: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Cập nhật giao diện ngay lập tức cho mượt
                val currentList = _notifications.value.toMutableList()
                val index = currentList.indexOfFirst { it.notificationId == notificationId }
                if (index != -1 && currentList[index].isRead == false) {
                    currentList[index] = currentList[index].copy(isRead = true)
                    _notifications.value = currentList
                    _unreadCount.value = currentList.count { it.isRead == false }
                }

                // 2. Đẩy trạng thái đã đọc lên Database
                client.from("notifications").update(
                    { set("isRead", true) }
                ) {
                    filter { eq("notificationId", notificationId) }
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi cập nhật trạng thái đọc: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = client.auth.currentSessionOrNull()?.user ?: return@launch

                // 1. Cập nhật giao diện nội bộ ngay lập tức cho mượt
                val currentList = _notifications.value.map { it.copy(isRead = true) }
                _notifications.value = currentList
                _unreadCount.value = 0 // Xóa sạch số trên chuông

                // 2. Cập nhật trên Database Supabase
                client.from("notifications").update(
                    { set("isRead", true) }
                ) {
                    filter {
                        eq("accountId", user.id)
                        eq("isRead", false) // Chỉ cập nhật những cái chưa đọc để tối ưu
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi đọc toàn bộ: ${e.message}")
            }
        }
    }
}