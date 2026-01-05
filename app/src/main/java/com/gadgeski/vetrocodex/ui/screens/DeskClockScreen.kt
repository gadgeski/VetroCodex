package com.gadgeski.vetrocodex.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gadgeski.vetrocodex.ui.components.BigTimeDisplay
import com.gadgeski.vetrocodex.ui.viewmodel.ClockViewModel
import com.gadgeski.vetrocodex.util.HingePosture
import com.gadgeski.vetrocodex.util.rememberHingePosture
import kotlin.random.Random

/**
 * 時計画面のルート
 *
 * - ヒンジ状態（半開きかどうか）を検知し、レイアウトを自動調整します。
 * - 【New】焼き付き防止（Pixel Shift / Scale）ロジックを追加
 */
@Composable
fun DeskClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()

    // ヒンジの状態を監視 (FLAT or HALF_OPENED)
    val hingePosture by rememberHingePosture()

    // ------------------------------------------------------------
    // 焼き付き防止 (Burn-in Protection) ロジック
    // ------------------------------------------------------------

    // 1. Pixel Shift: 位置をずらすオフセット量 (px単位だとデバイス依存なのでdpで管理し、offset修飾子で適用)
    // 1分ごとに更新するため、currentTime.minute をキーにします
    var shiftOffsetX by remember { mutableStateOf(0.dp) }
    var shiftOffsetY by remember { mutableStateOf(0.dp) }

    // 2. Scale Breathing: サイズを微妙に変える
    // 10分ごとに更新するため、(currentTime.minute / 10) をキーにします
    var breathingScale by remember { mutableFloatStateOf(1.0f) }

    // 時刻の「分」が変わるたびにピクセルシフトを実行
    LaunchedEffect(currentTime.minute) {
        // 上下左右に -8dp 〜 +8dp の範囲でランダムに移動
        // ※ あまり大きく動かすと、画面端で見切れる可能性があるので控えめに
        shiftOffsetX = Random.nextInt(-8, 9).dp
        shiftOffsetY = Random.nextInt(-8, 9).dp

        // 10分おきにスケールを変更 (0.95f 〜 1.05f)
        if (currentTime.minute % 10 == 0) {
            // 0.95 + (0.00 ~ 0.10) -> 0.95 ~ 1.05
            breathingScale = 0.95f + (Random.nextFloat() * 0.10f)
        }
    }

    // ------------------------------------------------------------
    // レイアウト制御
    // ------------------------------------------------------------

    // 半開き状態なら、画面の「上半分(50%)」だけを使う。通常なら「全部(100%)」使う。
    val heightFraction by animateFloatAsState(
        targetValue = if (hingePosture == HingePosture.HALF_OPENED) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "heightAnimation"
    )

    // 半開き時は上寄せ、全開時は中央
    val contentAlignment = if (hingePosture == HingePosture.HALF_OPENED) {
        Alignment.TopCenter
    } else {
        Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // 黒背景
        contentAlignment = contentAlignment
    ) {
        // 時計表示エリア
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(heightFraction), // 1.0f -> 0.5f
            contentAlignment = Alignment.Center
        ) {
            BigTimeDisplay(
                time = currentTime,
                modifier = Modifier
                    .fillMaxSize()
                    // 【適用】 焼き付き防止：スケールとオフセットを適用
                    // アニメーションさせずパッと切り替えるのがPixel Shiftの定石です
                    // （じわじわ動くと逆に全画素をなめることになりかねないため）
                    .scale(breathingScale)
                    .offset(x = shiftOffsetX, y = shiftOffsetY)
            )
        }
    }
}