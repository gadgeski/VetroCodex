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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val (blurRadius, tintColor) = getWeatherVisuals(weatherState)

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

        // コンテンツ全体をラップするBoxを作成し、ここでオフセットを適用
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { burnInOffset }
        // 1分ごとに微妙に位置がずれる
        ) {
            if (isLandscape) {
                // ■ 横画面
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
                // ■ 縦画面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = 250.dp)
                    ) {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        WeatherDisplay(weatherState = weatherState)
                    }

                    GlassText(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge,
                        bgImage = backgroundImage,
                        blurRadius = blurRadius,
                        tintColor = tintColor,
                        modifier = Modifier
                            .offset(y = (-30).dp)
                    )
                }
            }
        }
    }
}

fun getWeatherVisuals(state: WeatherUiState): Pair<Float, Color> {
    if (state !is WeatherUiState.Success) return 40f to Color.White

    val id = state.weather.weather.firstOrNull()?.id ?: 800
    return when (id) {
        in 200..232 -> 50f to Color(0xFFE1BEE7)
        // Thunderstorm (Purple-ish)
        in 300..321 -> 60f to Color(0xFFB3E5FC)
        // Drizzle (Light Blue)
        in 500..531 -> 60f to Color(0xFF81D4FA)
        // Rain (Blue)
        in 600..622 -> 50f to Color(0xFFE0F7FA)
        // Snow (Cyan-ish White)
        in 701..781 -> 30f to Color(0xFFCFD8DC)
        // Atmosphere (Mist - Greyish)
        800 -> 30f to Color(0xFFFFF9C4)
        // Clear (Yellow-ish White)
        in 801..804 -> 40f to Color.White
        // Clouds
        else -> 40f to Color.White
    }
}