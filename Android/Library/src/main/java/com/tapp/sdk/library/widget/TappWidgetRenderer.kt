package com.tapp.sdk.library.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.LruCache
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import com.tapp.sdk.library.R
import com.tapp.sdk.library.internal.logD
import java.io.File

internal object TappWidgetRenderer {

    private const val BITMAP_CACHE_SIZE_BYTES = 8 * 1024 * 1024
    private const val NEUTRAL_BACKGROUND_BITMAP_SIZE_PIXELS = 32
    private const val NEUTRAL_WHEEL_BITMAP_SIZE_PIXELS = 500
    private const val FALLBACK_BITMAP_SIZE_PIXELS = 1

    private val bitmapCache = object : LruCache<String, Bitmap>(BITMAP_CACHE_SIZE_BYTES) {

        override fun sizeOf(
            key: String,
            value: Bitmap
        ): Int {
            return value.byteCount
        }
    }

    private val bitmapPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG
    )
    private val neutralWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(32, 38, 48)
        style = Paint.Style.FILL
    }
    private val neutralWheelFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(170, 176, 186)
        style = Paint.Style.STROKE
        strokeWidth = 24f
    }

    fun createRemoteViews(
        context: Context,
        appWidgetIdentifier: Int,
        widgetAssetFiles: TappWidgetAssetFiles,
        wheelRotationDegrees: Float
    ): RemoteViews {
        return RemoteViews(
            context.packageName,
            R.layout.tapp_widget_layout
        ).apply {
            setImageViewBitmap(
                R.id.tapp_widget_background_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.background,
                    fallbackBitmap = ::createNeutralBackgroundBitmap
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_wheel_image,
                createRotatedWheelBitmap(
                    assetFile = widgetAssetFiles.wheel,
                    wheelRotationDegrees = wheelRotationDegrees
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_wheel_frame_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.wheelFrame,
                    fallbackBitmap = ::createNeutralWheelFrameBitmap
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_spin_button_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.wheelSpin,
                    fallbackBitmap = {
                        loadBitmap(
                            context = context,
                            drawableResourceIdentifier = R.drawable.tapp_widget_wheel_spin
                        )
                    }
                )
            )
            setOnClickPendingIntent(
                R.id.tapp_widget_spin_button_image,
                TappWidgetIntentFactory.createSpinPendingIntent(
                    context = context,
                    appWidgetIdentifier = appWidgetIdentifier
                )
            )
        }
    }

    private fun createRotatedWheelBitmap(
        assetFile: File?,
        wheelRotationDegrees: Float
    ): Bitmap {
        val wheelBitmap = loadAssetBitmap(
            assetFile = assetFile,
            fallbackBitmap = ::createNeutralWheelBitmap
        ) ?: return createBitmap(
            width = FALLBACK_BITMAP_SIZE_PIXELS,
            height = FALLBACK_BITMAP_SIZE_PIXELS
        )
        val rotatedWheelBitmap = createBitmap(
            width = wheelBitmap.width,
            height = wheelBitmap.height
        )
        val canvas = Canvas(rotatedWheelBitmap)

        canvas.withRotation(
            wheelRotationDegrees,
            wheelBitmap.width / 2f,
            wheelBitmap.height / 2f
        ) {
            drawBitmap(wheelBitmap, 0f, 0f, bitmapPaint)
        }

        return rotatedWheelBitmap
    }

    private fun loadAssetBitmap(
        assetFile: File?,
        fallbackBitmap: () -> Bitmap?
    ): Bitmap? {
        val assetBitmap = assetFile?.let(::loadBitmap)

        if (assetFile != null && assetBitmap == null) {
            logD(
                "failed to decode asset file: " +
                    "fileName=${assetFile.name}, " +
                    "localPath=${assetFile.absolutePath}"
            )
        }

        return assetBitmap ?: fallbackBitmap()
    }

    private fun createNeutralBackgroundBitmap(): Bitmap {
        return loadCachedBitmap("generated:neutralBackground") {
            createBitmap(
                width = NEUTRAL_BACKGROUND_BITMAP_SIZE_PIXELS,
                height = NEUTRAL_BACKGROUND_BITMAP_SIZE_PIXELS
            ).apply {
                eraseColor(Color.rgb(11, 18, 34))
            }
        } ?: createBitmap(width = FALLBACK_BITMAP_SIZE_PIXELS, height = FALLBACK_BITMAP_SIZE_PIXELS)
    }

    private fun createNeutralWheelBitmap(): Bitmap {
        return loadCachedBitmap("generated:neutralWheel") {
            createBitmap(
                width = NEUTRAL_WHEEL_BITMAP_SIZE_PIXELS,
                height = NEUTRAL_WHEEL_BITMAP_SIZE_PIXELS
            ).apply {
                val radius = width * 0.42f
                Canvas(this).drawCircle(
                    width / 2f,
                    height / 2f,
                    radius,
                    neutralWheelPaint
                )
            }
        } ?: createBitmap(width = FALLBACK_BITMAP_SIZE_PIXELS, height = FALLBACK_BITMAP_SIZE_PIXELS)
    }

    private fun createNeutralWheelFrameBitmap(): Bitmap {
        return loadCachedBitmap("generated:neutralWheelFrame") {
            createBitmap(
                width = NEUTRAL_WHEEL_BITMAP_SIZE_PIXELS,
                height = NEUTRAL_WHEEL_BITMAP_SIZE_PIXELS
            ).apply {
                val padding = neutralWheelFramePaint.strokeWidth / 2f + 4f
                Canvas(this).drawOval(
                    RectF(
                        padding,
                        padding,
                        width - padding,
                        height - padding
                    ),
                    neutralWheelFramePaint
                )
            }
        } ?: createBitmap(width = FALLBACK_BITMAP_SIZE_PIXELS, height = FALLBACK_BITMAP_SIZE_PIXELS)
    }

    private fun loadBitmap(assetFile: File): Bitmap? {
        val cacheKey = assetFile.toBitmapCacheKey()

        return loadCachedBitmap(cacheKey) {
            runCatching {
                BitmapFactory.decodeFile(
                    assetFile.absolutePath,
                    BitmapFactory.Options().apply {
                        inScaled = false
                    }
                )
            }.getOrNull()
        }
    }

    private fun loadBitmap(
        context: Context,
        drawableResourceIdentifier: Int
    ): Bitmap? {
        val cacheKey = "drawable:$drawableResourceIdentifier"

        return loadCachedBitmap(cacheKey) {
            runCatching {
                BitmapFactory.decodeResource(
                    context.resources,
                    drawableResourceIdentifier,
                    BitmapFactory.Options().apply {
                        inScaled = false
                    }
                )
            }.getOrNull()
        }
    }

    private fun loadCachedBitmap(
        cacheKey: String,
        decodeBitmap: () -> Bitmap?
    ): Bitmap? {
        synchronized(bitmapCache) {
            bitmapCache.get(cacheKey)?.let { cachedBitmap ->
                if (!cachedBitmap.isRecycled) {
                    return cachedBitmap
                }
            }
        }

        val decodedBitmap = decodeBitmap() ?: return null

        synchronized(bitmapCache) {
            bitmapCache.put(cacheKey, decodedBitmap)
        }

        return decodedBitmap
    }

    private fun File.toBitmapCacheKey(): String {
        return "file:$absolutePath:${lastModified()}:${length()}"
    }
}
