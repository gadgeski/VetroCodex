package com.gadgeski.vetro

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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

        // Edge-to-Edge 有効化
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        // 常時点灯モード
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            VetroTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val currentMode by viewModel.currentMode.collectAsState()

                // 設定ダイアログの表示状態
                var showSettings by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 画面全体に対するジェスチャー検知
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    // 長押しで設定画面を開く
                                    showSettings = true
                                }
                            )
                        }
                ) {
                    // モードに応じた画面表示
                    when (currentMode) {
                        ClockMode.MINIMAL -> DeskClockScreen()
                        ClockMode.CYBERPUNK -> CyberpunkClockScreen()
                    }

                    // 設定ダイアログの表示
                    if (showSettings) {
                        SettingsDialog(
                            currentMode = currentMode,
                            onModeSelected = { newMode ->
                                viewModel.selectMode(newMode)
                                showSettings = false // 選択したら閉じる
                            },
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }
}