package com.example.vetro.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.vetro.data.WeatherResponse
import com.example.vetro.ui.viewmodel.WeatherUiState
import kotlin.math.roundToInt

@Composable
fun WeatherDisplay(
    weatherState: WeatherUiState,
    modifier: Modifier = Modifier
) {
    if (weatherState is WeatherUiState.Success) {
        val weather = weatherState.weather
        val temp = weather.main.temp.roundToInt()
        val conditionId = weather.weather.firstOrNull()?.id ?: 800
        val icon = getWeatherIcon(conditionId)

        Row(
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = weather.weather.firstOrNull()?.description,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$temp°",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

fun getWeatherIcon(id: Int): ImageVector {
    return when (id) {
        in 200..232 -> Icons.Filled.FlashOn // Thunderstorm
        in 300..321 -> Icons.Filled.Grain // Drizzle
        in 500..531 -> Icons.Filled.Grain // Rain
        in 600..622 -> Icons.Filled.AcUnit // Snow
        in 701..781 -> Icons.Filled.BlurOn // Atmosphere (Mist, etc)
        800 -> Icons.Filled.WbSunny // Clear
        in 801..804 -> Icons.Filled.Cloud // Clouds
        else -> Icons.Filled.WbSunny
    }
}
