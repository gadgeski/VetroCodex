package com.gadgeski.vetro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gadgeski.vetro.R

// 1. カスタムフォントファミリーの定義 (BBH Bartle)
// ※ R.font.bbh_bartle が未解決になる場合、app/src/main/res/font/ に bbh_bartle.ttf を配置してください
val BBHBartleFontFamily = FontFamily(
    Font(R.font.bbh_bartle, FontWeight.Normal)
)

// 2. タイポグラフィの設定
val Typography = Typography(
    // 時計の「時」「分」表示用 (Display Large)
    // 画面いっぱいに表示するため、非常に大きなサイズを定義します
    displayLarge = TextStyle(
        fontFamily = BBHBartleFontFamily,
        fontWeight = FontWeight.Normal,
        // ここでのサイズは基準値です。実際のUIでは画面幅に合わせてスケーリングします
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

    /* * その他のスタイル（bodyLargeなど）はデフォルトのままにします。
     * 設定画面などは標準フォントの方が読みやすいためです。
     */
)