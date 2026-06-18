package com.example.endangeredanimals.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.Account
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val _topUsers = MutableStateFlow<List<Account>>(emptyList())
    val topUsers = _topUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = SupabaseInstance.client.from("accounts")
                    .select {
                        filter {
                            gt("score", 0) // Chỉ lấy những người có trên 0 điểm
                        }
                        order("score", order = Order.DESCENDING)
                        limit(10)
                    }
                    .decodeList<Account>()

                _topUsers.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}