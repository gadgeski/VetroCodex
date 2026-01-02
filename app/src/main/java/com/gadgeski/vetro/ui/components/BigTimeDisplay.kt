package com.gadgeski.vetro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Vetroのメイン機能：特大時計表示コンポーネント
 *
 * - BBH Bartleフォントを使用
 * - 画面幅に合わせてフォントサイズを動的に最大化
 * - 時間を鮮やかな色に変更
 * - constraints.maxWidth を使用して計算を最適化
 */
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
// IDEの誤検知警告を抑制
@Composable
fun BigTimeDisplay(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    val hourString = time.format(DateTimeFormatter.ofPattern("HH"))
    val minuteString = time.format(DateTimeFormatter.ofPattern("mm"))

    // 警告回避のため LocalDensity は外で取得
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        // 【最適化】
        // maxWidth(dp) を toPx() するのではなく、constraints.maxWidth(px) を直接使用します。
        // これにより無駄な変換計算がなくなり、IDEの誤検知も回避しやすくなります。
        val constraintsWidth = constraints.maxWidth

        // フォントサイズ計算（メモ化）
        val dynamicFontSize = remember(constraintsWidth, density) {
            with(density) {
                // constraintsWidth は Int(px) なのでそのまま計算に使用
                (constraintsWidth * 0.65f).toSp()
            }
        }

        // 行間計算（メモ化）
        val dynamicLineHeight = remember(dynamicFontSize) {
            dynamicFontSize * 0.85f
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 時 (Hour) - 鮮やかな色 (Cyber Cyan)
            Text(
                text = hourString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = dynamicFontSize,
                    lineHeight = dynamicLineHeight
                ),
                color = Color(0xFF00FFFF),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )

            // 分 (Minute) - グレー
            Text(
                text = minuteString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = dynamicFontSize,
                    lineHeight = dynamicLineHeight
                ),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
            )
        }
    }
}