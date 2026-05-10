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
import kotlinx.serialization.json.JsonElement
import org.json.JSONArray
import java.io.InputStream
import java.util.UUID

class ContributionViewModel : ViewModel() {

    private val _images = MutableStateFlow<List<ContributionImage>>(emptyList())
    val images: StateFlow<List<ContributionImage>> = _images.asStateFlow()

    // BIẾN STATE THEO DÕI QUÁ TRÌNH UPLOAD
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

    // --- HÀM MỚI: UPLOAD LÊN SUPABASE ---
    fun uploadContributions(context: Context, description: String, aiResult: String?, onSuccess: () -> Unit) {
        val validImages = _images.value.filter { it.isValid && !it.isLoading }
        if (validImages.isEmpty()) return

        _isUploading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = SupabaseInstance.client.auth.currentSessionOrNull()
                val accountId = session?.user?.id ?: throw Exception("Bạn chưa đăng nhập!")

                // 1. KHAI BÁO CÁC BIẾN Ở NGOÀI CÙNG ĐỂ GIỮ STATE
                var finalStatus = "pending"
                var finalAiPrediction: String? = null
                var finalAnimalId: String? = null

                // THÊM 2 BIẾN NÀY ĐỂ DÀNH CHO BÌNH LUẬN AI
                var aiTop1Species: String? = null
                var aiTop1Confidence: Int = 0

                // 2. BỘ NÃO XỬ LÝ
                if (!aiResult.isNullOrBlank()) {
                    try {
                        val jsonArray = JSONArray(aiResult)
                        if (jsonArray.length() > 0) {
                            val top1 = jsonArray.getJSONObject(0)

                            // Gán giá trị cho 2 biến global vừa tạo
                            aiTop1Confidence = top1.getInt("confidence")
                            aiTop1Species = top1.getString("speciesName").substringBefore(" (")

                            if (aiTop1Confidence >= 85) {
                                finalStatus = "approved"
                                finalAnimalId = aiTop1Species
                                finalAiPrediction = null
                            } else {
                                finalStatus = "discussing"
                                finalAnimalId = null
                                finalAiPrediction = aiResult
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        finalStatus = "pending"
                        finalAiPrediction = aiResult
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

                    val newContribution = Contribution(
                        contributionId = UUID.randomUUID().toString(),
                        accountId = accountId,
                        imageUrl = publicUrl,
                        latitude = null,
                        longitude = null,
                        aiPrediction = finalAiPrediction as JsonElement?,
                        status = finalStatus,
                        userNote = description.ifBlank { null },
                        finalAnimalId = finalAnimalId,
                        createdAt = null
                    )

                    // Insert bài đăng gốc
                    SupabaseInstance.client.from("contributions").insert(newContribution)

                    // 4. CHÈN BÌNH LUẬN AI (NẾU CÓ)
                    // Dùng biến aiTop1Species và aiTop1Confidence ở đây thì sẽ không bao giờ lỗi
                    if (finalStatus == "discussing" && aiTop1Species != null) {
                        val aiComment = CommunityDiscussion(
                            discussionId = 0,
                            contributionId = newContribution.contributionId,
                            accountId = "SYSTEM_AI",
                            comment = "AI dự đoán đây là: $aiTop1Species (Độ tự tin: ${aiTop1Confidence}%).",
                            suggestedAnimalId = null, // Chờ user vote
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
}