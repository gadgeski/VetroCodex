package com.gadgeski.vetrocodex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Vetroは「常にダークモード（黒背景）」の世界観なので、
// Light/Darkの両方で同じ黒基調のスキームを定義し、Purpleなどの色は排除します。

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

private val LightColorScheme = darkColorScheme(
    // ライトモード設定の端末でも、黒背景を強制します
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

@Composable
fun VetroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamicColor 引数は不要になったため削除しました
    content: @Composable () -> Unit
) {
    // 常にDarkColorScheme的な黒配色を使用します
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 以前ここにあった window.statusBarColor の設定は削除しました。
    // SDK 35/36以降は MainActivity で enableEdgeToEdge を使用するのが標準です。

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Type.kt の定義（BBH Bartle）を適用
        content = content
    )
}