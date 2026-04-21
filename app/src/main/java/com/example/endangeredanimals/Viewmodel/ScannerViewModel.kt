package com.example.endangeredanimals.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val imageUri: Uri? = null,
    val aiResult: String? = null,
    val isLoading: Boolean = false
)

class ScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Hàm gọi khi người dùng chọn ảnh từ thư viện
    fun onImageSelected(uri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(
                imageUri = uri,
                aiResult = null // Reset kết quả khi có ảnh mới
            )
        }
    }

    // Hàm giả lập phân tích AI
    fun analyzeImage() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // TODO: Sau này bạn thay chỗ này bằng logic gọi API/Model AI thật
            delay(2000) // Giả lập thời gian AI chạy (2 giây)

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    aiResult = "Sao La (Pseudoryx nghetinhensis)\nĐộ tự tin: 98%"
                )
            }
        }
    }
}