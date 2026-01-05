package com.gadgeski.vetrocodex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Vetroのメイン機能：特大時計表示コンポーネント
 *
 * - BBH Bartleフォントを使用
 * - 画面幅に合わせてフォントサイズを動的に最大化
 * - 時間を鮮やかな色に変更
 * - 【Update】非対称サイバーパンク型デザイン
 *   時（Hour）を大きく、分（Minute）をやや控えめにして視覚的ヒエラルキーを構築
 */
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
fun BigTimeDisplay(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    val hourString = time.format(DateTimeFormatter.ofPattern("HH"))
    val minuteString = time.format(DateTimeFormatter.ofPattern("mm"))

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val constraintsWidth = constraints.maxWidth
        val constraintsHeight = constraints.maxHeight

        // 時（Hour）のフォントサイズ - やや大きめ
        val hourFontSize = remember(constraintsWidth, constraintsHeight, density) {
            with(density) {
                val sizeByWidth = constraintsWidth * 0.42f
                val sizeByHeight = constraintsHeight * 0.42f
                min(sizeByWidth, sizeByHeight).toSp()
            }
        }

        // 分（Minute）のフォントサイズ - 時より小さめ
        val minuteFontSize = remember(constraintsWidth, constraintsHeight, density) {
            with(density) {
                val sizeByWidth = constraintsWidth * 0.35f
                val sizeByHeight = constraintsHeight * 0.35f
                min(sizeByWidth, sizeByHeight).toSp()
            }
        }

        val hourLineHeight = remember(hourFontSize) {
            hourFontSize * 0.80f
        }

        val minuteLineHeight = remember(minuteFontSize) {
            minuteFontSize * 0.80f
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            // 時 (Hour) - 左寄せ、大きめ、Cyan
            Text(
                text = hourString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = hourFontSize,
                    lineHeight = hourLineHeight
                ),
                color = Color(0xFF00FFFF), // Cyber Cyan
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )

            // 分 (Minute) - 右寄せ、やや小さめ、Gray
            Text(
                text = minuteString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = minuteFontSize,
                    lineHeight = minuteLineHeight
                ),
                color = Color.Gray,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp) // 適度な間隔
            )
        }
    }
}