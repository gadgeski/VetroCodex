// VetroWidgetReceiver.kt
package com.example.vetro.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VetroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VetroWidget()

    companion object {
        const val ACTION_UPDATE_TICK = "com.example.vetro.ACTION_UPDATE_TICK"
        private const val REQUEST_CODE = 1001
        private const val TAG = "VetroWidgetReceiver"

        // 更新間隔（ミリ秒）
        // 注意: Dozeモード中はシステムによって間引かれる可能性あり
        private const val UPDATE_INTERVAL_MS = 60_000L

        // アラームのバッファ（00秒ちょうどを狙うための微調整）
        private const val ALARM_BUFFER_MS = 50L
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled. Scheduling updates.")
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget disabled. Canceling updates.")
        cancelUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_TICK) {
            Log.d(TAG, "Alarm received. Starting update...")

            val pendingResult = goAsync()

            // SupervisorJobを追加して、子コルーチンの失敗が親に影響しないようにする
            CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                try {
                    updateWidget(context)
                    Log.d(TAG, "Update finished successfully.")
                } catch (e: Exception) {
                    Log.e(TAG, "Update failed", e)
                } finally {
                    // 成功・失敗に関わらず、次のアラームをセットしてからfinish
                    scheduleNextUpdate(context)
                    pendingResult.finish()
                    Log.d(TAG, "PendingResult finished. Next alarm scheduled.")
                }
            }
        }
    }

    /**
     * ウィジェットの表示を更新する
     */
    private suspend fun updateWidget(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(VetroWidget::class.java)

        if (glanceIds.isEmpty()) {
            Log.w(TAG, "No widget instances found.")
            return
        }

        glanceIds.forEach { glanceId ->
            try {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[VetroWidget.KEY_LAST_UPDATE] = System.currentTimeMillis()
                    }
                }
                glanceAppWidget.update(context, glanceId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget: $glanceId", e)
                // 個別のウィジェット更新失敗は握りつぶして、他のウィジェットの更新を続行
            }
        }
    }

    /**
     * 次の分の00秒にアラームをセットする
     */
    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available")
            return
        }

        val intent = Intent(context, VetroWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val nextTriggerTime = now + UPDATE_INTERVAL_MS - (now % UPDATE_INTERVAL_MS) + ALARM_BUFFER_MS

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
            Log.d(TAG, "Next alarm scheduled at: $nextTriggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Cannot schedule exact alarm. Check SCHEDULE_EXACT_ALARM permission.", e)
        }
    }

    private fun cancelUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available")
            return
        }

        val intent = Intent(context, VetroWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm canceled.")
    }
}