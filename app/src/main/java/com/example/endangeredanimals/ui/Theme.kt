package com.example.endangeredanimals.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // -- Màu chính (App Bar, Nút bấm lớn, Icon chính) --
    primary = Green500,
    onPrimary = Color.White,          // Chữ trên nền primary

    // Màu cho vùng chọn, container nhạt màu
    primaryContainer = Green100,      // Nền phụ màu xanh nhạt
    onPrimaryContainer = Green900,    // Chữ màu xanh đậm nằm trong container trên
    secondaryContainer = Neutral200,
    onSecondaryContainer = Neutral800,

    // -- Màu phụ (FAB, Điểm nhấn, Nút đổi mật khẩu) --
    secondary = AccentBrown,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    onTertiary = Color.White,

    // -- Màu nền (Toàn màn hình & Thẻ Card) --
    background = Neutral50,           // Nền màn hình chính
    onBackground = Neutral800,        // Màu chữ chính
    surface = Color.White,            // Nền của thẻ Card, BottomSheet
    onSurface = Neutral800,           // Màu chữ trên thẻ Card
    surfaceVariant = Neutral100,      // Dùng cho nền của TextField hoặc Card xám nhạt
    onSurfaceVariant = Neutral600,    // Dùng cho Hint Text, Text phụ

    // -- Trạng thái --
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun EndangeredAnimalsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}