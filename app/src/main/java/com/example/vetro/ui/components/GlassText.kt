package com.example.vetro.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle

/**
 * GlassText
 * 背景画像をすりガラス状にぼかして、テキストの形状で切り抜くコンポーネント。
 */
@Composable
fun GlassText(
    text: String,
    style: TextStyle,
    bgImage: ImageBitmap,
    modifier: Modifier = Modifier,
    blurRadius: Float = 40f,
    tintColor: Color = Color.White
) {
    // Android 12 (API 31) 未満へのフォールバック（単なる半透明白文字）
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Text(
            text = text,
            style = style.copy(color = Color.White.copy(alpha = 0.5f)),
            modifier = modifier
        )
        return
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // --- レイヤー1: マスク処理 (背景をぼかして文字型に切り抜く) ---
        Box(
            modifier = Modifier.graphicsLayer {
                // このBox内だけで合成計算を完結させる（重要）
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            // A. 切り抜く型（テキスト）
            Text(
                text = text,
                style = style,
                color = Color.Black, // 色はマスク用なので何でも良い
                modifier = Modifier.align(Alignment.Center)
            )

            // B. 埋め込む素材（ぼかした背景画像）
            Image(
                bitmap = bgImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.MIRROR)
                            .asComposeRenderEffect()

                        // 【エラー修正箇所】
                        // BlendModeのインポートがあれば、このプロパティは正しく認識されます。
                        this.blendMode = BlendMode.SrcIn
                    }
            )
        }

        // --- レイヤー2: 質感（ハイライトと輪郭） ---
        Text(
            text = text,
            style = style.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.7f), // 左上: 明るい反射
                        tintColor.copy(alpha = 0.1f)  // 右下: 透け感
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}