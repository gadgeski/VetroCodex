package com.gadgeski.vetro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStoreの拡張プロパティ（ファイル名: settings.preferences_pb）
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * ユーザー設定（モードなど）の永続化を担当するリポジトリ
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    // 【修正】警告回避のため、アノテーションのターゲットを明示的に「コンストラクタ引数(param)」に指定
    @param:ApplicationContext private val context: Context
) {
    // 保存するキーの定義
    private object PreferencesKeys {
        val CLOCK_MODE = stringPreferencesKey("clock_mode")
    }

    /**
     * 現在のクロックモードを監視するFlow
     * 初期値またはエラー時は MINIMAL (通常モード) を返します
     */
    val clockMode: Flow<ClockMode> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.CLOCK_MODE] ?: ClockMode.MINIMAL.name
            try {
                ClockMode.valueOf(modeString)
            } catch (_: IllegalArgumentException) {
                // 万が一変な値が入っていたらデフォルトに戻す
                ClockMode.MINIMAL
            }
        }

    /**
     * クロックモードを保存する
     */
    suspend fun setClockMode(mode: ClockMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOCK_MODE] = mode.name
        }
    }
}