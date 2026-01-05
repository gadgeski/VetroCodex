package com.gadgeski.vetrocodex.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gadgeski.vetrocodex.R

// 1. カスタムフォントファミリーの定義 (BBH Bartle)
val BBHBartleFontFamily = FontFamily(
    Font(R.font.bbh_bartle, FontWeight.Normal)
)

// 【追加】 Orbitron Font Family (Cyberpunk用)
// ※ app/src/main/res/font/ に orbitron_bold.ttf と orbitron_medium.ttf を配置してください
val OrbitronFontFamily = FontFamily(
    Font(R.font.orbitron_bold, FontWeight.Bold),
    Font(R.font.orbitron_medium, FontWeight.Medium)
)

// 2. タイポグラフィの設定
val Typography = Typography(
    // 時計の「時」「分」表示用 (Display Large) - BBH Bartle (通常モード用)
    displayLarge = TextStyle(
        fontFamily = BBHBartleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 120.sp,
        lineHeight = 120.sp,
        letterSpacing = (-2).sp
    ),
    // 日付や補足情報用 (Display Medium)
    displayMedium = TextStyle(
        fontFamily = BBHBartleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    )
)