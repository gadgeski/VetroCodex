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

@Suppress("unused")
object VetroWidgetRenderer {

    private const val TAG = "VetroWidgetRenderer"

    // 【修正】引数の数を VetroWidget.kt からの呼び出しに合わせて修正 (wallpaperIndexを追加)
    fun renderWidgetBitmap(context: Context, width: Float, height: Float, wallpaperIndex: Int = 0): Bitmap {
        Log.d(TAG, "Start rendering widget: w=$width, h=$height, idx=$wallpaperIndex")

        val w = if (width > 0) width.toInt() else 300
        val h = if (height > 0) height.toInt() else 300

        val bitmap = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. 背景画像の描画
        val bgBitmap = getWallpaperOrFallback(context, w, h, wallpaperIndex)

        if (bgBitmap != null) {
            val srcRect = getCenterCropRect(bgBitmap.width, bgBitmap.height, w, h)
            val dstRect = Rect(0, 0, w, h)
            val paint = Paint().apply { isFilterBitmap = true }
            canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
        } else {
            canvas.drawColor(Color.DKGRAY)
        }

        // 2. 時刻と日付の描画
        drawClockFace(context, canvas, w.toFloat(), h.toFloat())

        return bitmap
    }

    private fun drawClockFace(context: Context, canvas: Canvas, width: Float, height: Float) {
        val timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 (E)"))

        val timeTypeface = try {
            ResourcesCompat.getFont(context, R.font.custom_thin_font)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
        val dateTypeface = Typeface.DEFAULT_BOLD

        val targetWidth = width * 0.9f
        val testPaint = Paint().apply {
            this.typeface = timeTypeface
            this.textSize = 100f
        }
        val textWidth = testPaint.measureText(timeText)
        val safeTextWidth = if (textWidth > 0) textWidth else 1f
        val scaleFactor = targetWidth / safeTextWidth
        val timeFontSize = 100f * scaleFactor

        val centerX = width / 2f
        val timeFontMetrics = Paint().apply { this.textSize = timeFontSize; this.typeface = timeTypeface }.fontMetrics
        val timeTextHeight = timeFontMetrics.descent - timeFontMetrics.ascent

        val timeY = (height / 2f) + (timeTextHeight / 3f)

        val dateFontSize = timeFontSize * 0.13f
        val dateOffsetY = -20f
        val dateY = timeY - timeTextHeight + timeFontMetrics.descent - (dateFontSize * 0.8f) + dateOffsetY

        val datePaint = Paint().apply {
            this.typeface = dateTypeface
            this.textSize = dateFontSize
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
            this.color = Color.WHITE
            setShadowLayer(10f, 0f, 0f, Color.argb(150, 0, 0, 0))
        }
        canvas.drawText(dateText, centerX, dateY, datePaint)

        val fillPaint = Paint().apply {
            this.typeface = timeTypeface
            this.textSize = timeFontSize
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
            this.style = Paint.Style.FILL
            this.shader = LinearGradient(
                0f, timeY - timeTextHeight, 0f, timeY,
                Color.argb(200, 255, 255, 255),
                Color.argb(20, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawText(timeText, centerX, timeY, fillPaint)

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

    private fun getWallpaperOrFallback(context: Context, reqW: Int, reqH: Int, wallpaperIndex: Int): Bitmap? {
        var bitmap: Bitmap? = null

        if (wallpaperIndex == 0) {
            // パターン0: システム壁紙
            bitmap = try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                @SuppressLint("MissingPermission")
                val drawable = try { wallpaperManager.drawable } catch(e:Exception) { null }
                drawable?.toBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap == null) {
            // パターン1またはフォールバック
            try {
                val resourceId = if (wallpaperIndex == 1) {
                    R.drawable.background_img_2
                } else {
                    R.drawable.background_img
                }

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(context.resources, resourceId, options)
                options.inSampleSize = calculateInSampleSize(options, reqW, reqH)
                options.inJustDecodeBounds = false
                bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            } catch (e: Exception) {
                Log.e(TAG, "Image load failed for index $wallpaperIndex", e)
                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.background_img)
            }
        }

        if (bitmap != null) {
            return scaleBitmapToFit(bitmap, reqW, reqH)
        }

        return null
    }

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

    private fun scaleBitmapToFit(bitmap: Bitmap, reqW: Int, reqH: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= reqW * 1.5 && height <= reqH * 1.5) return bitmap
        val scale = max(reqW.toFloat() / width, reqH.toFloat() / height)
        val newW = (width * scale).roundToInt()
        val newH = (height * scale).roundToInt()
        return try {
            bitmap.scale(newW, newH, true)
        } catch (e: Exception) {
            bitmap
        }
    }

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