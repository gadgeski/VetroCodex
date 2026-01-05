package com.gadgeski.vetrocodex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/**
 * 時計のロジックを管理するViewModel
 *
 * - 現在時刻の保持
 * - 1秒ごとの更新ループ
 * - 将来的には焼き付き防止用のオフセット計算などもここで行う想定
 */
@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {

    // UIの状態として現在時刻を公開
    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    init {
        startTicking()
    }

    private fun startTicking() {
        viewModelScope.launch {
            while (isActive) {
                _currentTime.value = LocalTime.now()
                // 次の00秒（または更新タイミング）まで待機する計算を入れるのが厳密ですが、
                // 置き時計としてはシンプルな delay で十分機能します。
                // 負荷を下げるため、毎フレームではなく適度な間隔で更新します。
                delay(1000L)
            }
        }
    }
}