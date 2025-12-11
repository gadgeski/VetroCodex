// app/src/main/java/com/example/vetro/ui/screen/ClockScreen.kt

package com.example.vetro.ui.screen

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetro.R
import com.example.vetro.ui.components.GlassText
import com.example.vetro.ui.components.WeatherDisplay
import com.example.vetro.ui.util.rememberSystemWallpaper
import com.example.vetro.ui.viewmodel.ClockViewModel
import com.example.vetro.ui.viewmodel.WeatherUiState

@Composable
fun ClockScreen(
    viewModel: ClockViewModel = viewModel()
) {
    val timeString by viewModel.timeString.collectAsState()
    val dateString by viewModel.dateString.collectAsState()
    val burnInOffset by viewModel.burnInOffset.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    // 権限リクエスト
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.fetchWeather()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    // 天気に応じたビジュアル変化
    val (blurRadius, tintColor) = remember(weatherState) {
        getWeatherVisuals(weatherState)
    }

    val systemWallpaper: ImageBitmap? = rememberSystemWallpaper()
    val backgroundImage: ImageBitmap = systemWallpaper ?: ImageBitmap.imageResource(id = R.drawable.background_img)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景描画 (背景はずらさず固定)
        Image(
            bitmap = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // コンテンツ全体をラップするBoxを作成し、ここで焼き付き防止オフセットを適用
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { burnInOffset }
        ) {
            if (isLandscape) {
                // ■ 横画面レイアウト
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge,
                        bgImage = backgroundImage,
                        blurRadius = blurRadius,
                        tintColor = tintColor,
                        // weightを使うことで画面サイズに合わせて適切にスペースを分配
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        WeatherDisplay(weatherState = weatherState)
                    }
                }
            } else {
                // ■ 縦画面レイアウト
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // 下部のスペース確保
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // SpacerとWeightを使って中央より少し上に配置するなど、柔軟に調整
                    Spacer(modifier = Modifier.weight(1f))

                    GlassText(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge,
                        bgImage = backgroundImage,
                        blurRadius = blurRadius,
                        tintColor = tintColor,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        WeatherDisplay(weatherState = weatherState)
                    }

                    Spacer(modifier = Modifier.weight(1.2f)) // 下を少し広めにとる
                }
            }
        }
    }
}

// ロジック部分: 天気IDに基づいてブラー強度と色味を決定
private fun getWeatherVisuals(state: WeatherUiState): Pair<Float, Color> {
    if (state !is WeatherUiState.Success) return 40f to Color.White

    // OpenWeatherMap Condition Codes
    val id = state.weather.weather.firstOrNull()?.id ?: 800
    return when (id) {
        // Thunderstorm (Purple-ish)
        in 200..232 -> 50f to Color(0xFFE1BEE7)
        // Drizzle (Light Blue)
        in 300..321 -> 60f to Color(0xFFB3E5FC)
        // Rain (Blue)
        in 500..531 -> 60f to Color(0xFF81D4FA)
        // Snow (Cyan-ish White)
        in 600..622 -> 50f to Color(0xFFE0F7FA)
        // Atmosphere (Mist - Greyish)
        in 701..781 -> 30f to Color(0xFFCFD8DC)
        // Clear (Yellow-ish White)
        800 -> 30f to Color(0xFFFFF9C4)
        // Clouds
        in 801..804 -> 40f to Color.White
        else -> 40f to Color.White
    }
}