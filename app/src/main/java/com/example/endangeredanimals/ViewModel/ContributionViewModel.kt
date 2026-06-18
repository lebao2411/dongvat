package com.example.endangeredanimals.ViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.endangeredanimals.Component.AnimalAiService
import com.example.endangeredanimals.Component.SupabaseInstance
import com.example.endangeredanimals.Model.CommunityDiscussion
import com.example.endangeredanimals.Model.Contribution
import com.example.endangeredanimals.Model.ContributionInsert
import com.example.endangeredanimals.View.ContributionImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// THÊM 3 IMPORT CỦA BỘ THƯ VIỆN KOTLINX JSON
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json

import java.io.InputStream
import java.util.UUID

class ContributionViewModel : ViewModel() {

    private val _images = MutableStateFlow<List<ContributionImage>>(emptyList())
    val images: StateFlow<List<ContributionImage>> = _images.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val aiService by lazy { AnimalAiService() }

    fun addImages(context: Context, uris: List<Uri>) {
        val newImages = uris.map { ContributionImage(uri = it, isLoading = true) }
        _images.update { it + newImages }

        newImages.forEach { image ->
            validateImage(context, image.uri)
        }
    }

    fun removeImage(uri: Uri) {
        _images.update { currentList -> currentList.filter { it.uri != uri } }
    }

    fun uploadContributions(context: Context, description: String, aiResult: String?, onSuccess: () -> Unit) {
        val validImages = _images.value.filter { it.isValid && !it.isLoading }
        if (validImages.isEmpty()) return

        _isUploading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = SupabaseInstance.client.auth.currentSessionOrNull()
                val accountId = session?.user?.id ?: throw Exception("Bạn chưa đăng nhập!")

                var finalStatus = "pending"
                // CHÚ Ý: Biến này giờ mang chuẩn kiểu JsonElement của thư viện
                var finalAiPrediction: JsonElement? = null
                var finalAnimalId: String? = null

                var aiTop1Species: String? = null
                var aiTop1Confidence: Int = 0

                // 2. BỘ NÃO XỬ LÝ (ĐÃ ĐƯỢC CẬP NHẬT ĐỂ ĐỌC CHỮ TIẾNG VIỆT)
                if (!aiResult.isNullOrBlank()) {
                    try {
                        val lines = aiResult.lines()
                        val speciesLine = lines.find { it.startsWith("Tên loài:") }
                        val confLine = lines.find { it.startsWith("Độ tự tin:") }

                        if (speciesLine != null && confLine != null) {
                            // Cắt chữ ra để lấy Tên và %
                            val fullSpecies = speciesLine.substringAfter("Tên loài:").trim()
                            aiTop1Species = fullSpecies.substringBefore(" (").trim()

                            val confStr = confLine.substringAfter("Độ tự tin:").replace("%", "").trim()
                            aiTop1Confidence = confStr.toIntOrNull() ?: 0

                            if (aiTop1Confidence >= 85) {
                                finalStatus = "approved"
                                finalAnimalId = aiTop1Species
                                finalAiPrediction = null
                            } else {
                                finalStatus = "discussing"
                                finalAnimalId = null

                                // TÁI TẠO LẠI CẤU TRÚC JSON để màn hình Thảo luận có thể đọc được
                                val reconstructedJson = """[{"speciesName": "$fullSpecies", "confidence": $aiTop1Confidence}]"""
                                finalAiPrediction = Json.parseToJsonElement(reconstructedJson)
                            }
                        } else {
                            finalStatus = "pending"
                            finalAiPrediction = JsonPrimitive(aiResult)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        finalStatus = "pending"
                        finalAiPrediction = JsonPrimitive(aiResult)
                    }
                }

                // 3. Upload từng ảnh lên Storage và lưu Database
                for (image in validImages) {
                    val inputStream = context.contentResolver.openInputStream(image.uri)
                    val bytes = inputStream?.readBytes() ?: continue
                    inputStream.close()

                    val fileName = "${accountId}_${System.currentTimeMillis()}.jpg"

                    SupabaseInstance.client.storage.from("contribution_images").upload(fileName, bytes)
                    val publicUrl = SupabaseInstance.client.storage.from("contribution_images").publicUrl(fileName)

                    val location = getExifLocation(context, image.uri)
                    val imgLat = location?.first
                    val imgLon = location?.second
                    val capturedTime = getPhotoCapturedTime(context, image.uri)

                    val newContribution = ContributionInsert(
                        contributionId = UUID.randomUUID().toString(),
                        accountId = accountId,
                        imageUrl = publicUrl,
                        latitude = imgLat,
                        longitude = imgLon,
                        aiPrediction = finalAiPrediction,
                        status = finalStatus,
                        userNote = description.ifBlank { null },
                        finalAnimalId = finalAnimalId,
                        capturedAt = capturedTime
                    )

                    SupabaseInstance.client.from("contributions").insert(newContribution)

                    // 4. CHÈN BÌNH LUẬN CỦA HỆ THỐNG AI
                    if (finalStatus == "discussing" && aiTop1Species != null) {
                        val aiComment = CommunityDiscussion(
                            discussionId = 0,
                            contributionId = newContribution.contributionId,
                            accountId = "SYSTEM_AI",
                            comment = "AI dự đoán đây là: $aiTop1Species (Độ tự tin: ${aiTop1Confidence}%).",
                            suggestedAnimalId = null,
                            createdAt = null
                        )
                        SupabaseInstance.client.from("community_discussions").insert(aiComment)
                    }
                }

                withContext(Dispatchers.Main) {
                    _isUploading.value = false
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isUploading.value = false
                }
            }
        }
    }

    private fun validateImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val hasExif = withContext(Dispatchers.IO) { checkExifData(context, uri) }
                if (!hasExif) {
                    updateImageStatus(uri, isValid = false, "Ảnh thiếu dữ liệu gốc (Có thể là ảnh copy từ mạng).")
                    return@launch
                }

                val bitmap = withContext(Dispatchers.IO) { uriToBitmap(context, uri) }
                if (bitmap == null) {
                    updateImageStatus(uri, isValid = false, "Không thể đọc định dạng ảnh này.")
                    return@launch
                }

                val isAnimal = aiService.validateIsAnimal(bitmap)
                if (!isAnimal) {
                    updateImageStatus(uri, isValid = false, "AI không tìm thấy động vật trong ảnh này.")
                } else {
                    updateImageStatus(uri, isValid = true, null)
                }
            } catch (e: Exception) {
                updateImageStatus(uri, isValid = false, "Lỗi xử lý: ${e.message}")
            }
        }
    }

    private fun updateImageStatus(uri: Uri, isValid: Boolean, errorMsg: String?) {
        _images.update { currentList ->
            currentList.map {
                if (it.uri == uri) it.copy(isLoading = false, isValid = isValid, errorMessage = errorMsg)
                else it
            }
        }
    }

    private fun checkExifData(context: Context, uri: Uri): Boolean {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                dateTime != null
            } else false
        } catch (e: Exception) {
            false
        } finally {
            inputStream?.close()
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
            null
        }
    }

    // Trích xuất Kinh độ và Vĩ độ từ ảnh
    private fun getExifLocation(context: Context, uri: Uri): Pair<Double, Double>? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val latLong = FloatArray(2)

                // Hàm getLatLong sẽ tự động lấy và ép kiểu tọa độ GPS vào mảng latLong
                if (exif.getLatLong(latLong)) {
                    Pair(latLong[0].toDouble(), latLong[1].toDouble()) // Trả về (Vĩ độ, Kinh độ)
                } else {
                    null // Ảnh không có lưu vị trí
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    private fun getPhotoCapturedTime(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val rawDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

                if (!rawDateTime.isNullOrBlank()) {
                    // Chuyển đổi từ định dạng EXIF sang định dạng chuẩn ISO của Supabase
                    val parser = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.getDefault())
                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    val date = parser.parse(rawDateTime)
                    if (date != null) formatter.format(date) else null
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }
}