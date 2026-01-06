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
 * 時計画面のルート (Minimal Mode)
 *
 * - ヒンジ状態（半開きかどうか）を検知し、レイアウトを自動調整します。
 * - 焼き付き防止（Pixel Shift / Scale）ロジックを搭載
 */
@Composable
fun DeskClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()

    // ヒンジの状態を監視
    val hingePosture by rememberHingePosture()

    // ------------------------------------------------------------
    // 焼き付き防止 (Burn-in Protection) ロジック
    // ------------------------------------------------------------

    var shiftOffsetX by remember { mutableStateOf(0.dp) }
    var shiftOffsetY by remember { mutableStateOf(0.dp) }
    var breathingScale by remember { mutableFloatStateOf(1.0f) }

    LaunchedEffect(currentTime.minute) {
        // Pixel Shift
        shiftOffsetX = Random.nextInt(-8, 9).dp
        shiftOffsetY = Random.nextInt(-8, 9).dp

        // Breathing Scale
        if (currentTime.minute % 10 == 0) {
            breathingScale = 0.95f + (Random.nextFloat() * 0.10f)
        }
    }

    // ------------------------------------------------------------
    // レイアウト制御
    // ------------------------------------------------------------

    // 【修正】 HALF_OPENED を TABLETOP_MODE に変更
    // Minimalモードは上下分割レイアウトなので、TABLETOP_MODE (水平ヒンジ半開き) の時に反応させます。
    // ※ BOOK_MODE (垂直ヒンジ) の時は、Minimalモードでは全画面表示 (FLAT扱い) とします。
    val isTabletop = hingePosture == HingePosture.TABLETOP_MODE

    // 半開き状態なら、画面の「上半分(50%)」だけを使う。通常なら「全部(100%)」使う。
    val heightFraction by animateFloatAsState(
        targetValue = if (isTabletop) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "heightAnimation"
    )

    // 半開き時は上寄せ、全開時は中央
    val contentAlignment = if (isTabletop) {
        Alignment.TopCenter
    } else {
        Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        // 黒背景
        contentAlignment = contentAlignment
    ) {
        // 時計表示エリア
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(heightFraction),
        // 1.0f -> 0.5f
            contentAlignment = Alignment.Center
        ) {
            BigTimeDisplay(
                time = currentTime,
                modifier = Modifier
                    .fillMaxSize()
                    // 焼き付き防止適用
                    .scale(breathingScale)
                    .offset(x = shiftOffsetX, y = shiftOffsetY)
            )
        }
    }
}