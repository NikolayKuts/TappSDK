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
        val bitmapTargetSizes = TappWidgetBitmapTargetSizes.create(context)

        return RemoteViews(
            context.packageName,
            R.layout.tapp_widget_layout
        ).apply {
            setImageViewBitmap(
                R.id.tapp_widget_background_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.background,
                    targetSize = bitmapTargetSizes.background,
                    fallbackBitmap = ::createNeutralBackgroundBitmap
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_wheel_image,
                createRotatedWheelBitmap(
                    assetFile = widgetAssetFiles.wheel,
                    targetSize = bitmapTargetSizes.wheel,
                    wheelRotationDegrees = wheelRotationDegrees
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_wheel_frame_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.wheelFrame,
                    targetSize = bitmapTargetSizes.frame,
                    fallbackBitmap = ::createNeutralWheelFrameBitmap
                )
            )
            setImageViewBitmap(
                R.id.tapp_widget_spin_button_image,
                loadAssetBitmap(
                    assetFile = widgetAssetFiles.wheelSpin,
                    targetSize = bitmapTargetSizes.spinButton,
                    fallbackBitmap = { targetSize ->
                        loadBitmap(
                            context = context,
                            drawableResourceIdentifier = R.drawable.tapp_widget_wheel_spin,
                            targetSize = targetSize
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
        targetSize: TappWidgetBitmapTargetSize,
        wheelRotationDegrees: Float
    ): Bitmap {
        val wheelBitmap = loadAssetBitmap(
            assetFile = assetFile,
            targetSize = targetSize,
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
        targetSize: TappWidgetBitmapTargetSize,
        fallbackBitmap: (TappWidgetBitmapTargetSize) -> Bitmap?
    ): Bitmap? {
        val assetBitmap = assetFile?.let { file ->
            loadBitmap(
                assetFile = file,
                targetSize = targetSize
            )
        }

        if (assetFile != null && assetBitmap == null) {
            logD(
                "failed to decode asset file: " +
                    "fileName=${assetFile.name}, " +
                    "localPath=${assetFile.absolutePath}"
            )
        }

        return assetBitmap ?: fallbackBitmap(targetSize)
    }

    private fun createNeutralBackgroundBitmap(targetSize: TappWidgetBitmapTargetSize): Bitmap {
        return loadCachedBitmap("generated:neutralBackground:${targetSize.cacheKey}") {
            createBitmap(
                width = targetSize.width,
                height = targetSize.height
            ).apply {
                eraseColor(Color.rgb(11, 18, 34))
            }
        } ?: createBitmap(width = FALLBACK_BITMAP_SIZE_PIXELS, height = FALLBACK_BITMAP_SIZE_PIXELS)
    }

    private fun createNeutralWheelBitmap(targetSize: TappWidgetBitmapTargetSize): Bitmap {
        return loadCachedBitmap("generated:neutralWheel:${targetSize.cacheKey}") {
            createBitmap(
                width = targetSize.width,
                height = targetSize.height
            ).apply {
                val radius = minOf(width, height) * 0.42f
                Canvas(this).drawCircle(
                    width / 2f,
                    height / 2f,
                    radius,
                    neutralWheelPaint
                )
            }
        } ?: createBitmap(width = FALLBACK_BITMAP_SIZE_PIXELS, height = FALLBACK_BITMAP_SIZE_PIXELS)
    }

    private fun createNeutralWheelFrameBitmap(targetSize: TappWidgetBitmapTargetSize): Bitmap {
        return loadCachedBitmap("generated:neutralWheelFrame:${targetSize.cacheKey}") {
            createBitmap(
                width = targetSize.width,
                height = targetSize.height
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

    private fun loadBitmap(
        assetFile: File,
        targetSize: TappWidgetBitmapTargetSize
    ): Bitmap? {
        val cacheKey = assetFile.toBitmapCacheKey(targetSize)

        return loadCachedBitmap(cacheKey) {
            runCatching {
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                BitmapFactory.decodeFile(assetFile.absolutePath, boundsOptions)

                decodeScaledBitmap(
                    sourceWidth = boundsOptions.outWidth,
                    sourceHeight = boundsOptions.outHeight,
                    decodeBitmap = { inSampleSize ->
                        BitmapFactory.decodeFile(
                            assetFile.absolutePath,
                            BitmapFactory.Options().apply {
                                this.inSampleSize = inSampleSize
                                inScaled = false
                            }
                        )
                    },
                    targetSize = targetSize
                )
            }.getOrNull()
        }
    }

    private fun loadBitmap(
        context: Context,
        drawableResourceIdentifier: Int,
        targetSize: TappWidgetBitmapTargetSize
    ): Bitmap? {
        val cacheKey = "drawable:$drawableResourceIdentifier:${targetSize.cacheKey}"

        return loadCachedBitmap(cacheKey) {
            runCatching {
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                BitmapFactory.decodeResource(
                    context.resources,
                    drawableResourceIdentifier,
                    boundsOptions
                )

                decodeScaledBitmap(
                    sourceWidth = boundsOptions.outWidth,
                    sourceHeight = boundsOptions.outHeight,
                    decodeBitmap = { inSampleSize ->
                        BitmapFactory.decodeResource(
                            context.resources,
                            drawableResourceIdentifier,
                            BitmapFactory.Options().apply {
                                this.inSampleSize = inSampleSize
                                inScaled = false
                            }
                        )
                    },
                    targetSize = targetSize
                )
            }.getOrNull()
        }
    }

    private fun decodeScaledBitmap(
        sourceWidth: Int,
        sourceHeight: Int,
        decodeBitmap: (Int) -> Bitmap?,
        targetSize: TappWidgetBitmapTargetSize
    ): Bitmap? {
        val decodedBitmap = decodeBitmap(
            calculateInSampleSize(
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                targetWidth = targetSize.width,
                targetHeight = targetSize.height
            )
        ) ?: return null

        if (
            decodedBitmap.width == targetSize.width &&
            decodedBitmap.height == targetSize.height
        ) {
            return decodedBitmap
        }

        val scaledBitmap = Bitmap.createScaledBitmap(
            decodedBitmap,
            targetSize.width,
            targetSize.height,
            true
        )

        decodedBitmap.recycle()

        return scaledBitmap
    }

    private fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var inSampleSize = 1
        var halfSourceWidth = sourceWidth / 2
        var halfSourceHeight = sourceHeight / 2

        while (
            halfSourceWidth / inSampleSize >= targetWidth &&
            halfSourceHeight / inSampleSize >= targetHeight
        ) {
            inSampleSize *= 2
        }

        return inSampleSize
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

    private fun File.toBitmapCacheKey(targetSize: TappWidgetBitmapTargetSize): String {
        return "file:$absolutePath:${lastModified()}:${length()}:${targetSize.cacheKey}"
    }

    private data class TappWidgetBitmapTargetSizes(
        val background: TappWidgetBitmapTargetSize,
        val wheel: TappWidgetBitmapTargetSize,
        val frame: TappWidgetBitmapTargetSize,
        val spinButton: TappWidgetBitmapTargetSize
    ) {

        companion object {

            fun create(context: Context): TappWidgetBitmapTargetSizes {
                return TappWidgetBitmapTargetSizes(
                    background = TappWidgetBitmapTargetSize(
                        width = context.getDimensionPixels(R.dimen.tapp_widget_min_width),
                        height = context.getDimensionPixels(R.dimen.tapp_widget_min_height)
                    ),
                    wheel = TappWidgetBitmapTargetSize.square(
                        context.getDimensionPixels(R.dimen.tapp_widget_wheel_size)
                    ),
                    frame = TappWidgetBitmapTargetSize.square(
                        context.getDimensionPixels(R.dimen.tapp_widget_frame_size)
                    ),
                    spinButton = TappWidgetBitmapTargetSize(
                        width = context.getDimensionPixels(R.dimen.tapp_widget_spin_button_width),
                        height = context.getDimensionPixels(R.dimen.tapp_widget_spin_button_height)
                    )
                )
            }
        }
    }

    private data class TappWidgetBitmapTargetSize(
        val width: Int,
        val height: Int
    ) {

        val cacheKey: String = "${width}x$height"

        companion object {

            fun square(size: Int): TappWidgetBitmapTargetSize {
                return TappWidgetBitmapTargetSize(
                    width = size,
                    height = size
                )
            }
        }
    }

    private fun Context.getDimensionPixels(dimensionResourceIdentifier: Int): Int {
        return resources.getDimensionPixelSize(dimensionResourceIdentifier)
            .coerceAtLeast(1)
    }
}
