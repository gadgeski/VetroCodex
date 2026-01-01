package com.gadgeski.vetro

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gadgeski.vetro.ui.theme.VetroTheme
import dagger.hilt.android.AndroidEntryPoint

// 削除: import com.gadgeski.vetro.ui.screen.ClockScreen

@AndroidEntryPoint // Hiltを使用可能にするアノテーション
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 常時点灯モード (Keep Screen On)
        // これはリニューアル後も必須機能なので残します
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            VetroTheme {
                // 背景を黒（Black）に設定して、Vetroの世界観のベースを作ります
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    // ClockScreen() は削除しました。
                    // 代わりに、ビルド確認用の仮テキストを画面中央に表示します。
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Vetro Renewal Ready",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}