// MainActivity.kt
package com.gadgeski.vetro

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gadgeski.vetro.ui.screen.ClockScreen
import com.gadgeski.vetro.ui.theme.VetroTheme

// 【追加】 常時点灯用(android.view.WindowManager)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 【追加】 常時点灯モード (Keep Screen On)
        // これにより、アプリを開いている間はスリープ（画面消灯）しなくなります
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            // プロジェクト作成時に自動生成されるThemeでラップする
            VetroTheme {
                // Modifierを使うためには import androidx.compose.ui.Modifier が必要
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClockScreen()
                }
            }
        }
    }
}