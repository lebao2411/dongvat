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
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.Component.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class ScannerUiState(
    val imageUri: Uri? = null,
    val aiResult: String? = null,
    val isLoading: Boolean = false,
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
                matchedAnimalId = null
            )
        }
    }

    fun clearImage() {
        _uiState.update { ScannerUiState() }
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
                    val rawResultText = aiService.analyzeAnimalImage(bitmap)
                    var displayResult = rawResultText // Giá trị dự phòng nếu lỗi

                    // BÓC TÁCH JSON VÀ ĐỊNH DẠNG LẠI CHO NGƯỜI ĐỌC
                    try {
                        val jsonArray = JSONArray(rawResultText)
                        if (jsonArray.length() == 0) {
                            displayResult = "⚠️ Đây không phải là hình ảnh động vật."
                        } else {
                            // Tìm phần tử có độ tự tin cao nhất
                            var bestMatch = jsonArray.getJSONObject(0)
                            var maxConf = bestMatch.getInt("confidence")

                            for (i in 1 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val conf = item.getInt("confidence")
                                if (conf > maxConf) {
                                    maxConf = conf
                                    bestMatch = item
                                }
                            }

                            val speciesName = bestMatch.getString("speciesName")
                            val confidence = bestMatch.getInt("confidence")
                            val characteristics = bestMatch.getString("characteristics")
                            val note = bestMatch.getString("note")

                            // Định dạng đúng chuẩn để ScannerScreen có thể đọc được
                            displayResult = "Tên loài: $speciesName\n" +
                                    "Độ tự tin: $confidence%\n\n" +
                                    "Đặc điểm: $characteristics\n" +
                                    "Lưu ý: $note"
                        }
                    } catch (e: Exception) {
                        Log.e("ScannerVM", "Lỗi parse JSON: ${e.message}")
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            aiResult = displayResult
                        )
                    }

                    // Gọi tìm kiếm trong DB dựa trên chuỗi đã format
                    searchAnimalInDatabase(displayResult)

                } else {
                    _uiState.update { it.copy(isLoading = false, aiResult = "Lỗi: Không thể đọc được hình ảnh.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, aiResult = "Lỗi xử lý ảnh: ${e.message}") }
            }
        }
    }

    private fun searchAnimalInDatabase(aiResult: String) {
        val regex = Regex("""\(([^)]+)\)""")
        val matchResult = regex.find(aiResult)

        val scientificName = matchResult?.groups?.get(1)?.value ?: return

        _uiState.update { it.copy(isSearchingDb = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val animals = SupabaseInstance.client
                    .from("animals")
                    .select {
                        filter {
                            ilike("nameLatin", "%$scientificName%")
                        }
                    }
                    .decodeList<Animal>()

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