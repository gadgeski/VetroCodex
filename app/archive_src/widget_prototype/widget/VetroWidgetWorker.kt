// VetroWidgetWorker.kt → 廃止(更新システム入れ替え)
package com.gadgeski.vetro.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

@Suppress("unused")
class VetroWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // ウィジェットマネージャーを取得
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(VetroWidget::class.java)

        // 配置されている全てのVetroウィジェットに対して更新処理を行う
        glanceIds.forEach { glanceId ->
            // 1. ステート（状態）を強制的に更新する
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                // 現在時刻(ミリ秒)を保存して、強制的にステートを変更する
                prefs.toMutablePreferences().apply {
                    this[VetroWidget.KEY_LAST_UPDATE] = System.currentTimeMillis()
                }
            }
            // 2. ウィジェットの更新をリクエスト
            VetroWidget().update(context, glanceId)
        }

        return Result.success()
    }
}