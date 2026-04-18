package com.tapp.sdk.library.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import com.tapp.sdk.library.R

internal object TappWidgetRenderer {

    private val bitmapPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG
    )

    fun createRemoteViews(
        context: Context,
        appWidgetIdentifier: Int,
        wheelRotationDegrees: Float
    ): RemoteViews {
        return RemoteViews(
            context.packageName,
            R.layout.tapp_widget_layout
        ).apply {
            setImageViewBitmap(
                R.id.tapp_widget_wheel_image,
                createRotatedWheelBitmap(
                    context = context,
                    wheelRotationDegrees = wheelRotationDegrees
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
        context: Context,
        wheelRotationDegrees: Float
    ): Bitmap {
        val wheelBitmap = loadBitmap(
            context = context,
            drawableResourceIdentifier = R.drawable.tapp_widget_wheel
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

    private fun loadBitmap(
        context: Context,
        drawableResourceIdentifier: Int
    ): Bitmap? {
        return runCatching {
            BitmapFactory.decodeResource(
                context.resources,
                drawableResourceIdentifier,
                BitmapFactory.Options().apply {
                    inScaled = false
                }
            )
        }.getOrNull()
    }

    private const val FALLBACK_BITMAP_SIZE_PIXELS = 1
}
