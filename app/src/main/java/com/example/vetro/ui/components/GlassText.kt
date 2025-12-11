// app/src/main/java/com/example/vetro/ui/components/GlassText.kt

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
 * GlassText Component
 *
 * Android 12 (API 31) 以降の RenderEffect を活用し、
 * 背景画像をリアルタイムでぼかし、それをテキストの形状で切り抜く（Masking）ことで
 * すりガラス（Frosted Glass）のような視覚効果を実現します。
 *
 * @param bgImage 切り抜く対象となる背景画像（壁紙など）
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
    // API 31未満へのフォールバック: RenderEffectが使えないため、単なる半透明テキストを表示
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Text(
            text = text,
            style = style.copy(color = Color.White.copy(alpha = 0.5f)),
            modifier = modifier
        )
        return
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // --- Layer 1: Masking (Blur & Cutout) ---
        // 背景をぼかし、テキストの形に切り抜く処理
        Box(
            modifier = Modifier.graphicsLayer {
                // オフスクリーンバッファを使用し、このBox内だけで合成計算を完結させる。
                // これにより、BlendMode.SrcIn が親コンポーネント（背景）まで突き抜けるのを防ぐ。
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            // A. 切り抜く型（DST）: テキスト
            Text(
                text = text,
                style = style,
                color = Color.Black, // マスク用なので色は不問（アルファ値だけが重要）
                modifier = Modifier.align(Alignment.Center)
            )



            // B. 埋め込む素材（SRC）: ぼかした背景画像
            // BlendMode.SrcIn により、A（テキスト）と重なる部分だけが描画される
            Image(
                bitmap = bgImage,
                contentDescription = null,
                contentScale = ContentScale.Crop, // 画面いっぱいに広げて壁紙と位置を合わせる
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Android 12+ のぼかし効果を適用
                        renderEffect = RenderEffect
                            .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.MIRROR)
                            .asComposeRenderEffect()

                        // 重なり部分のみを描画
                        blendMode = BlendMode.SrcIn
                    }
            )
        }

        // --- Layer 2: Reflection & Highlight ---
        // ガラスの厚みや光の反射を表現するために、薄いグラデーションを上から重ねる
        Text(
            text = text,
            style = style.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.7f), // 左上: 強い反射（ハイライト）
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