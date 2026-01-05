package com.gadgeski.vetrocodex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gadgeski.vetrocodex.data.ClockMode
import com.gadgeski.vetrocodex.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * アプリ全体の状態（現在のモードなど）を管理するViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    // 現在のモードを監視するStateFlow
    // 初期値は MINIMAL ですが、DataStoreから読み込まれると即座に更新されます
    val currentMode: StateFlow<ClockMode> = repository.clockMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ClockMode.MINIMAL
        )

    /**
     * モードを変更して保存する
     */
    fun selectMode(mode: ClockMode) {
        viewModelScope.launch {
            repository.setClockMode(mode)
        }
    }
}