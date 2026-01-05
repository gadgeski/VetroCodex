package com.gadgeski.vetrocodex.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * 画面の折れ曲がり状態を表すクラス
 */
enum class HingePosture {
    FLAT,       // 完全に開いている (通常)
    HALF_OPENED // 半開き (L字/Tabletopモード)
}

/**
 * 現在の画面のヒンジ状態（姿勢）を監視するComposable関数
 *
 * @return 現在の [HingePosture]
 */
@Composable
fun rememberHingePosture(): State<HingePosture> {
    val context = LocalContext.current
    // Activityコンテキストが必要
    val activity = context as? Activity

    return produceState(initialValue = HingePosture.FLAT) {
        if (activity == null) return@produceState

        // WindowInfoTrackerを使ってレイアウト変更を監視
        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(activity)
            .collect { layoutInfo ->
                // FoldingFeature（折れ目）を探す
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                // 折れ目があり、かつ「半開き (BOOK or TABLETOP)」状態なら HALF_OPENED と判定
                value = if (foldingFeature != null && foldingFeature.state == FoldingFeature.State.HALF_OPENED) {
                    HingePosture.HALF_OPENED
                } else {
                    HingePosture.FLAT
                }
            }
    }
}