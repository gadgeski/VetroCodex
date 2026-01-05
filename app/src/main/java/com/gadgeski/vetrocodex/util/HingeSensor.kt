package com.gadgeski.vetrocodex.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * 画面の折れ曲がり状態を表すクラス (Fold対応版)
 * * Flip版では HALF_OPENED だけでしたが、Fold版では
 * 「本のように開く (BOOK)」と「PCのように開く (TABLETOP)」を区別します。
 */
enum class HingePosture {
    FLAT,         // 完全に開いている (通常)
    BOOK_MODE,    // 垂直ヒンジで半開き (Foldのメインスタイル / 左右分割)
    TABLETOP_MODE // 水平ヒンジで半開き (ノートPCスタイル / 上下分割)
}

/**
 * 現在の画面のヒンジ状態（姿勢）を監視するComposable関数
 *
 * @return 現在の [HingePosture]
 */
@Composable
fun rememberHingePosture(): State<HingePosture> {
    val context = LocalContext.current
    val activity = context as? Activity

    return produceState(initialValue = HingePosture.FLAT) {
        if (activity == null) return@produceState

        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(activity)
            .collect { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                value = if (foldingFeature != null && foldingFeature.state == FoldingFeature.State.HALF_OPENED) {
                    // ヒンジの向きでモードを分岐
                    if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
                        HingePosture.BOOK_MODE // 左右開き (Foldのメインスタイル)
                    } else {
                        HingePosture.TABLETOP_MODE // 上下開き
                    }
                } else {
                    HingePosture.FLAT
                }
            }
    }
}