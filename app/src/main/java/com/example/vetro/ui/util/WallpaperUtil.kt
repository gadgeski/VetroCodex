// app/src/main/java/com/example/vetro/ui/util/WallpaperUtil.kt

package com.example.vetro.ui.util

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * システムの壁紙を取得し、Compose用のImageBitmapとして返します。
 * 重いBitmap変換処理をIOスレッドで行うため、UIのジャンク（カクつき）を防ぎます。
 */
@Composable
fun rememberSystemWallpaper(): ImageBitmap? {
    val context = LocalContext.current

    // produceState を使うことで、非同期処理の結果を State として監視できます。
    // 初期値は null (取得完了までデフォルト画像を表示させるため)
    return produceState<ImageBitmap?>(initialValue = null, key1 = context) {
        // 重い処理なので IO スレッドに移動
        value = withContext(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)

                // =================================================================
                // 【注意】動く壁紙 (Live Wallpaper) に関する制約
                // =================================================================
                // ユーザーが「動く壁紙」を設定している場合、以下の挙動になる可能性があります。
                // 1. wallpaperManager.drawable が null を返す。
                // 2. 動く壁紙のサムネイル（静止画）が返される。
                // 3. メーカー独自のホームアプリの場合、アクセス権限で SecurityException が出る。
                //
                // そのため、必ず try-catch で保護し、呼び出し元で null の場合の
                // フォールバック画像（アプリ内デフォルト画像）を用意する設計にしています。
                // =================================================================

                // Lint抑制: 権限は呼び出し元(MainActivity/Screen)で管理しているため、ここでは抑制
                @SuppressLint("MissingPermission")
                val drawable = wallpaperManager.drawable

                if (drawable == null) {
                    null
                } else {
                    // Bitmap変換（ここが一番重い処理）
                    // 既にBitmapDrawableならそのまま取り出し、そうでなければ描画して変換
                    val bitmap = if (drawable is BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        drawable.toBitmap()
                    }
                    bitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                // 権限不足やLive Wallpaperの不整合などで取得できなかった場合は null を返す
                e.printStackTrace()
                null
            }
        }
    }
        .value
}