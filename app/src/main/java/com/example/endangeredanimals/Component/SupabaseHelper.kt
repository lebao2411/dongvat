package com.example.endangeredanimals.Component

import java.util.UUID

object SupabaseHelper {
    // 1. Base URL cho ảnh
    const val STORAGE_BASE_URL = "https://ehtlxhoymxclqevouozp.supabase.co/storage/v1/object/public/animal_images/"

    // 2. Tên các Bucket
    const val CONTRIBUTION_BUCKET = "contribution_images"

    // 3. Hàm xử lý Link ảnh (Áp dụng cho cả bảng Animal và Favorite)
    fun getFullImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        return STORAGE_BASE_URL + path
    }

    // 4. Hàm tạo UUID tự động (Dùng cho bảng Contribution hoặc User)
    fun generateUniqueId(): String = UUID.randomUUID().toString()
}