package com.gadgeski.vetro.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gadgeski.vetro.data.WeatherRepository
import com.gadgeski.vetro.data.WeatherRepositoryImpl
import com.gadgeski.vetro.data.WeatherResponse
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// 天気の状態を表すシールドクラス
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    object Error : WeatherUiState()
}

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val _timeString = MutableStateFlow("00:00")
    val timeString: StateFlow<String> = _timeString.asStateFlow()

    private val _dateString = MutableStateFlow("")
    val dateString: StateFlow<String> = _dateString.asStateFlow()

    // 【追加】焼き付き防止用のオフセット値 (X, Y)
    private val _burnInOffset = MutableStateFlow(IntOffset.Zero)
    val burnInOffset: StateFlow<IntOffset> = _burnInOffset.asStateFlow()

    // 【追加】天気の状態
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        startClock()
        startBurnInProtection()
        fetchWeather()
    }

    private fun startClock() {
        viewModelScope.launch {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val dateFormatter = DateTimeFormatter.ofPattern("M月d日 (E)")

            while (true) {
                val now = LocalTime.now()
                val today = java.time.LocalDate.now()

                _timeString.value = now.format(timeFormatter)
                _dateString.value = today.format(dateFormatter)

                delay(1000L)
            }
        }
    }

    // 【追加】焼き付き防止ロジック
    // 有機EL画面のために、1分ごとに表示位置を微妙にずらします
    private fun startBurnInProtection() {
        viewModelScope.launch {
            while (true) {
                // 上下左右に -10px ～ 10px の範囲でランダムに移動
                val x = Random.nextInt(-10, 11)
                val y = Random.nextInt(-10, 11)
                _burnInOffset.value = IntOffset(x, y)

                // 1分ごとに更新
                delay(60 * 1000L)
            }
        }
    }

    // 【追加】天気を取得する
    @SuppressLint("MissingPermission")
    // 呼び出し元で権限チェックを行う前提、またはtry-catchで握りつぶす
    fun fetchWeather() {
        viewModelScope.launch {
            try {
                // 位置情報の取得を試みる
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        getWeatherFromApi(location.latitude, location.longitude)
                    } else {
                        // 位置情報が取れない場合はデフォルト（東京）を表示するか、エラーにする
                        // ここではデフォルトとして東京の座標を使用
                        getWeatherFromApi(35.6895, 139.6917)
                    }
                }.addOnFailureListener {
                    // 失敗時もデフォルトへ
                    getWeatherFromApi(35.6895, 139.6917)
                }
            } catch (_: SecurityException) {
                // 権限がない場合など
                getWeatherFromApi(35.6895, 139.6917)
            }
        }
    }

    private fun getWeatherFromApi(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getCurrentWeather(lat, lon)
            result.onSuccess { response ->
                _weatherState.value = WeatherUiState.Success(response)
            }.onFailure {
                _weatherState.value = WeatherUiState.Error
            }
        }
    }
}