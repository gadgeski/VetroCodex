// Type.kt
package com.gadgeski.vetro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gadgeski.vetro.R

// res/font/custom_thin_font.ttf がある前提
// ない場合は FontFamily.Default に切り替わります
val CustomFontFamily = try {
    FontFamily(Font(R.font.custom_thin_font, FontWeight.Thin))
} catch (_: Exception) {
    // ★修正: "e" を "_" に変更
    FontFamily.Default
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 180.sp,
        letterSpacing = (-4).sp
    // 文字間を詰めてモダンに
    ),
    headlineMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        letterSpacing = 2.sp
    )
)