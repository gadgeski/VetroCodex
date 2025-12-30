// app/src/main/java/com/gadgeski/vetro/widget/VetroWidgetRenderer.kt

package com.gadgeski.vetro.widget

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.gadgeski.vetro.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * VetroWidgetRenderer
 *
 * RemoteViewsの制約を回避するため、バックグラウンドでCanvasを使用して
 * 「壁紙 + 時計 + ガラスエフェクト」を1枚のBitmapとして合成・生成するレンダラー。
 *
 * NOTE: この機能は実験的（Experimental）であり、現在のメイン機能はDaydreamServiceに移行しています。
 */
object VetroWidgetRenderer {

    private const val TAG = "VetroWidgetRenderer"

    /**
     * 指定されたサイズでウィジェット用のBitmapを生成します。
     */
    fun renderWidgetBitmap(context: Context, width: Float, height: Float): Bitmap {
        Log.d(TAG, "Start rendering widget: w=$width, h=$height")

        // ゼロサイズ対策
        val w = if (width > 0) width.toInt() else 300
        val h = if (height > 0) height.toInt() else 300

        val bitmap = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. 背景画像の描画（システム壁紙 or デフォルト画像）
        val bgBitmap = getWallpaperOrFallback(context, w, h)

        if (bgBitmap != null) {
            // 中央で切り抜いて描画（Center Crop）
            val srcRect = getCenterCropRect(bgBitmap.width, bgBitmap.height, w, h)
            val dstRect = Rect(0, 0, w, h)
            val paint = Paint().apply { isFilterBitmap = true }
            canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
        } else {
            // 最終フォールバック
            canvas.drawColor(Color.DKGRAY)
        }

        // 2. 時計盤面（テキストとエフェクト）の描画
        drawClockFace(context, canvas, w.toFloat(), h.toFloat())

        return bitmap
    }

    private fun drawClockFace(context: Context, canvas: Canvas, width: Float, height: Float) {
        val timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 (E)"))

        // カスタムフォントの読み込み
        val timeTypeface = try {
            ResourcesCompat.getFont(context, R.font.custom_thin_font)
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        val dateTypeface = Typeface.DEFAULT_BOLD

        // フォントサイズの動的計算（横幅の90%に収まるように調整）
        val targetWidth = width * 0.9f
        val testPaint = Paint().apply {
            this.typeface = timeTypeface
            this.textSize = 100f
        }
        val textWidth = testPaint.measureText(timeText)
        val safeTextWidth = if (textWidth > 0) textWidth else 1f
        val scaleFactor = targetWidth / safeTextWidth
        val timeFontSize = 100f * scaleFactor

        // 配置計算
        val centerX = width / 2f
        val timeFontMetrics = Paint().apply { this.textSize = timeFontSize; this.typeface = timeTypeface }.fontMetrics
        val timeTextHeight = timeFontMetrics.descent - timeFontMetrics.ascent
        val timeY = (height / 2f) + (timeTextHeight / 3f)

        val dateFontSize = timeFontSize * 0.13f
        val dateOffsetY = -20f
        val dateY = timeY - timeTextHeight + timeFontMetrics.descent - (dateFontSize * 0.8f) + dateOffsetY

        // --- A. 日付の描画 ---
        val datePaint = Paint().apply {
            this.typeface = dateTypeface
            this.textSize = dateFontSize
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
            this.color = Color.WHITE
            setShadowLayer(10f, 0f, 0f, Color.argb(150, 0, 0, 0))
        }
        canvas.drawText(dateText, centerX, dateY, datePaint)

        // --- B. 時刻の描画（グラデーション透過） ---
        val fillPaint = Paint().apply {
            this.typeface = timeTypeface
            this.textSize = timeFontSize
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
            this.style = Paint.Style.FILL
            // 上から下へ透明になるグラデーション
            this.shader = LinearGradient(
                0f, timeY - timeTextHeight, 0f, timeY,
                Color.argb(200, 255, 255, 255),
                Color.argb(20, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawText(timeText, centerX, timeY, fillPaint)

        // --- C. 時刻の描画（輪郭線・Stroke） ---
        // 輪郭を描くことで、背景が明るくても視認性を確保する
        val strokePaint = Paint().apply {
            this.typeface = timeTypeface
            this.textSize = timeFontSize
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
            this.style = Paint.Style.STROKE
            this.strokeWidth = timeFontSize * 0.015f
            this.color = Color.argb(180, 255, 255, 255)
            setShadowLayer(10f, 0f, 0f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawText(timeText, centerX, timeY, strokePaint)
    }

    /**
     * 壁紙を取得し、指定サイズに合わせてリサイズ・デコードして返します。
     * 取得できない場合はアプリ内リソースを返します。
     */
    private fun getWallpaperOrFallback(context: Context, reqW: Int, reqH: Int): Bitmap? {
        var bitmap: Bitmap? = null

        // 1. システム壁紙の取得を試みる
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            @SuppressLint("MissingPermission")
            val drawable = try { wallpaperManager.drawable } catch (_: Exception) { null }
            bitmap = drawable?.toBitmap()
        } catch (_: Exception) {
            // 権限エラーなどは無視してフォールバックへ
        }

        // 2. 失敗した場合はデフォルト画像を使用
        if (bitmap == null) {
            try {
                val resourceId = R.drawable.background_img
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(context.resources, resourceId, options)
                options.inSampleSize = calculateInSampleSize(options, reqW, reqH)
                options.inJustDecodeBounds = false
                bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            } catch (e: Exception) {
                Log.e(TAG, "Default image load failed", e)
            }
        }

        // 3. サイズ調整
        return if (bitmap != null) {
            scaleBitmapToFit(bitmap, reqW, reqH)
        } else {
            null
        }
    }

    // メモリ効率のための縮小率計算
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // アスペクト比を維持したリサイズ
    private fun scaleBitmapToFit(bitmap: Bitmap, reqW: Int, reqH: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        // 既に十分小さいならそのまま返す
        if (width <= reqW * 1.5 && height <= reqH * 1.5) return bitmap

        val scale = max(reqW.toFloat() / width, reqH.toFloat() / height)
        val newW = (width * scale).roundToInt()
        val newH = (height * scale).roundToInt()
        return try {
            bitmap.scale(newW, newH, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    // 中央切り抜き用のRect計算
    private fun getCenterCropRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcAspect = srcW.toFloat() / srcH
        val dstAspect = dstW.toFloat() / dstH
        return if (srcAspect > dstAspect) {
            val cropW = (srcH * dstAspect).toInt()
            val x = (srcW - cropW) / 2
            Rect(x, 0, x + cropW, srcH)
        } else {
            val cropH = (srcW / dstAspect).toInt()
            val y = (srcH - cropH) / 2
            Rect(0, y, srcW, y + cropH)
        }
    }
}