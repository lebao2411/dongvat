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
        // Chỉ lấy những ảnh hợp lệ và đã quét xong
        val validImages = _images.value.filter { it.isValid && !it.isLoading }
        if (validImages.isEmpty()) return

        _isUploading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Lấy ID người dùng hiện tại
                val session = SupabaseInstance.client.auth.currentSessionOrNull()
                val accountId = session?.user?.id ?: throw Exception("Bạn chưa đăng nhập!")

                // 2. Upload từng ảnh lên Storage và lưu Database
                for (image in validImages) {
                    val inputStream = context.contentResolver.openInputStream(image.uri)
                    val bytes = inputStream?.readBytes() ?: continue
                    inputStream.close()

                    // Tạo tên file duy nhất: accountId_timestamp.jpg
                    val fileName = "${accountId}_${System.currentTimeMillis()}.jpg"

                    // Ghi chú: Bạn phải tạo 1 bucket tên là "contribution_images" (để public) trên Supabase nhé
                    SupabaseInstance.client.storage.from("contribution_images").upload(fileName, bytes)
                    val publicUrl = SupabaseInstance.client.storage.from("contribution_images").publicUrl(fileName)

                    // 3. Khởi tạo Model để đẩy lên Database
                    val newContribution = Contribution(
                        contributionId = UUID.randomUUID().toString(), // Tạo ID ngẫu nhiên
                        accountId = accountId,
                        imageUrl = publicUrl,
                        latitude = null, // Có thể mở rộng lấy từ EXIF sau này
                        longitude = null,
                        aiPrediction = aiResult, // Truyền thẳng chuỗi AI vào
                        status = "pending",
                        userNote = description.ifBlank { null },
                        finalAnimalId = null,
                        createdAt = null // Để trống, Supabase sẽ tự động lấy giờ hiện tại
                    )

                    // Insert vào bảng
                    SupabaseInstance.client.from("contributions").insert(newContribution)
                }

                // Chuyển về luồng UI để báo hoàn thành
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