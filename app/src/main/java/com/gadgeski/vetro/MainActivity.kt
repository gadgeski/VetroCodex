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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

                // 【追加】画面の向きを検知
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 画面全体に対するジェスチャー検知 (長押しも残す)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    showSettings = true
                                }
                            )
                        }
                ) {
                    // 1. モードに応じた画面表示
                    when (currentMode) {
                        ClockMode.MINIMAL -> DeskClockScreen()
                        ClockMode.CYBERPUNK -> CyberpunkClockScreen()
                    }

                    // 2. 設定ボタン
                    // 横向き(Landscape)なら「左上」、それ以外(縦)なら「右上」に配置
                    // これにより、横画面時に右側の情報パネルと重なるのを防ぎます
                    val buttonAlignment = if (isLandscape) Alignment.TopStart else Alignment.TopEnd

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .align(buttonAlignment) // 動的に位置を変更
                            .padding(24.dp) // 角から少し離す
                            .size(48.dp) // タップ領域は大きめに確保
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.2f), // 亡霊のように薄くする
                            modifier = Modifier.fillMaxSize(0.6f) // アイコン自体の見た目は少し小さく
                        )
                    }

                    // 3. 設定ダイアログの表示
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