package com.example.endangeredanimals.ViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.AnimalAiService
import com.example.endangeredanimals.Model.Animal // Import Model của bạn
import com.example.endangeredanimals.Component.SupabaseInstance // Import Supabase của bạn
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScannerUiState(
    val imageUri: Uri? = null,
    val aiResult: String? = null,
    val isLoading: Boolean = false,

    // THÊM: Các trạng thái cho việc tìm kiếm Database
    val isSearchingDb: Boolean = false,
    val matchedAnimalId: String? = null
)

class ScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val aiService by lazy { AnimalAiService() }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(
                imageUri = uri,
                aiResult = null,
                matchedAnimalId = null // Reset khi chọn ảnh mới
            )
        }
    }

    // THÊM: Hàm xóa ảnh (Nút X)
    fun clearImage() {
        _uiState.update { ScannerUiState() } // Đưa mọi thứ về trạng thái tinh khôi
    }

    fun analyzeImage(context: Context) {
        val uri = _uiState.value.imageUri ?: return

        _uiState.update { it.copy(isLoading = true, matchedAnimalId = null) }

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    uriToBitmap(context, uri)
                }

                if (bitmap != null) {
                    val resultText = aiService.analyzeAnimalImage(bitmap)

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            aiResult = resultText
                        )
                    }

                    // TỰ ĐỘNG TÌM KIẾM TRONG DB KHI CÓ KẾT QUẢ AI
                    searchAnimalInDatabase(resultText)

                } else {
                    _uiState.update { it.copy(isLoading = false, aiResult = "Lỗi: Không thể đọc được hình ảnh.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, aiResult = "Lỗi xử lý ảnh: ${e.message}") }
            }
        }
    }

    // THÊM: Hàm móc tên khoa học và truy vấn DB
    private fun searchAnimalInDatabase(aiResult: String) {
        // Dùng Regex để lấy dòng chữ nằm giữa 2 dấu ngoặc đơn ()
        val regex = Regex("""\(([^)]+)\)""")
        val matchResult = regex.find(aiResult)

        // Nếu không tìm thấy tên khoa học (ví dụ AI trả lời lỗi), thì dừng lại
        val scientificName = matchResult?.groups?.get(1)?.value ?: return

        _uiState.update { it.copy(isSearchingDb = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Truy vấn Supabase: Lọc xem có con vật nào chứa tên khoa học này không
                val animals = SupabaseInstance.client
                    .from("animals")
                    .select {
                        filter {
                            ilike("nameLatin", "%$scientificName%") // ilike: Tìm kiếm không phân biệt hoa/thường
                        }
                    }
                    .decodeList<Animal>()

                // Lấy ID của con vật đầu tiên tìm thấy
                val foundAnimalId = animals.firstOrNull()?.animalID

                Log.d("ScannerVM", "Tên khoa học: $scientificName - ID tìm thấy: $foundAnimalId")
                _uiState.update { it.copy(isSearchingDb = false, matchedAnimalId = foundAnimalId) }

            } catch (e: Exception) {
                Log.e("ScannerVM", "Lỗi tìm DB: ${e.message}")
                _uiState.update { it.copy(isSearchingDb = false) }
            }
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}