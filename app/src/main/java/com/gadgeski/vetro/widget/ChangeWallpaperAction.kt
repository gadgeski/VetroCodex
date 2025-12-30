// ChangeWallpaperAction.kt
package com.gadgeski.vetro.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

class ChangeWallpaperAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // 現在の状態（インデックス）を取得して、0と1を切り替える
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[VetroWidget.KEY_WALLPAPER_INDEX] ?: 0

            // 【修正】 toMutablePreferences().apply { ... } を使うことで、
            // 変更後の Preferences オブジェクト自体を返却するようにします。
            // (以前は代入式の結果である Unit を返していたためエラーでした)
            prefs.toMutablePreferences().apply {
                this[VetroWidget.KEY_WALLPAPER_INDEX] = if (current == 0) 1 else 0
            }
        }
        // ウィジェットを更新（再描画）
        VetroWidget().update(context, glanceId)
    }
}