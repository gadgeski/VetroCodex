package com.gadgeski.vetro

import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gadgeski.vetro.data.ClockMode
import com.gadgeski.vetro.ui.components.SettingsDialog
import com.gadgeski.vetro.ui.screens.CyberpunkClockScreen
import com.gadgeski.vetro.ui.screens.DeskClockScreen
import com.gadgeski.vetro.ui.theme.VetroTheme
import com.gadgeski.vetro.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            VetroTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val currentMode by viewModel.currentMode.collectAsState()

                var showSettings by remember { mutableStateOf(false) }

                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    showSettings = true
                                }
                            )
                        }
                ) {
                    when (currentMode) {
                        ClockMode.MINIMAL -> DeskClockScreen()
                        ClockMode.CYBERPUNK -> CyberpunkClockScreen()
                    }

                    // 【修正】設定ボタンの配置ロジック
                    // モードと画面の向きに応じて、最も邪魔にならない「安全地帯」を選びます
                    val buttonAlignment = when (currentMode) {
                        // Minimalモード:
                        // 時(左上)・分(右下)のレイアウトなので、空いている「左下」がベストポジションです。
                        ClockMode.MINIMAL -> Alignment.BottomStart

                        // Cyberpunkモード:
                        // 横向きなら左上、縦向きなら右上（既存ロジック）
                        ClockMode.CYBERPUNK -> if (isLandscape) Alignment.TopStart else Alignment.TopEnd
                    }

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .align(buttonAlignment)
                            // システムバー（ナビゲーションバー等）と重ならないようインセットを考慮
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(24.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }

                    if (showSettings) {
                        SettingsDialog(
                            currentMode = currentMode,
                            onModeSelected = { newMode ->
                                viewModel.selectMode(newMode)
                                showSettings = false
                            },
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }
}