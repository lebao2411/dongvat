package com.example.endangeredanimals.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE0610D), // Màu từ theme.xml
    secondary = Color(0xFFab310f), // Màu từ theme.xml
    tertiary = Color(0xFF228cdb) // Màu từ theme.xml

)

@Composable
fun EndangeredAnimalsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography, // Có thể tùy chỉnh
        content = content
    )
}