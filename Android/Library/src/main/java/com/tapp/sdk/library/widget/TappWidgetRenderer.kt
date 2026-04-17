package com.tapp.sdk.library.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.tapp.sdk.library.R
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation

internal object TappWidgetRenderer {

    private const val WHEEL_BITMAP_SIZE_PIXELS = 640
    private const val FRAME_SIZE_RATIO = 0.90f
    private const val WHEEL_SIZE_RATIO_OF_FRAME = 0.805f
    private const val SPIN_BUTTON_WIDTH_RATIO_OF_FRAME = 0.34f
    private const val CONTENT_CENTER_VERTICAL_OFFSET_RATIO = 0.02f
    private const val WHEEL_CENTER_VERTICAL_OFFSET_RATIO_OF_FRAME = 0.005f

    private val bitmapPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG
    )

    fun createRemoteViews(
        context: Context,
        appWidgetIdentifier: Int,
        wheelRotationDegrees: Float
    ): RemoteViews {
        val wheelBitmap = createWheelBitmap(
            context = context,
            wheelRotationDegrees = wheelRotationDegrees
        )

        return RemoteViews(
            context.packageName,
            R.layout.tapp_widget_layout
        ).apply {
            setImageViewBitmap(R.id.tapp_widget_wheel_image, wheelBitmap)
            setOnClickPendingIntent(
                R.id.tapp_widget_root,
                TappWidgetIntentFactory.createSpinPendingIntent(
                    context = context,
                    appWidgetIdentifier = appWidgetIdentifier
                )
            )
        }
    }

    private fun createWheelBitmap(
        context: Context,
        wheelRotationDegrees: Float
    ): Bitmap {
        val outputBitmap = createBitmap(
            width = WHEEL_BITMAP_SIZE_PIXELS,
            height = WHEEL_BITMAP_SIZE_PIXELS
        )
        val canvas = Canvas(outputBitmap)
        val frameRectangle = createCenteredSquare(
            sizePixels = WHEEL_BITMAP_SIZE_PIXELS * FRAME_SIZE_RATIO
        )
        val wheelRectangle = createCenteredSquare(
            sizePixels = frameRectangle.width() * WHEEL_SIZE_RATIO_OF_FRAME,
            centerVerticalOffsetPixels = frameRectangle.height() *
                WHEEL_CENTER_VERTICAL_OFFSET_RATIO_OF_FRAME
        )

        drawWheel(
            context = context,
            canvas = canvas,
            wheelRectangle = wheelRectangle,
            wheelRotationDegrees = wheelRotationDegrees
        )
        drawWheelFrame(
            context = context,
            canvas = canvas,
            frameRectangle = frameRectangle
        )
        drawSpinButton(
            context = context,
            canvas = canvas,
            frameRectangle = frameRectangle
        )

        return outputBitmap
    }

    private fun drawWheel(
        context: Context,
        canvas: Canvas,
        wheelRectangle: RectF,
        wheelRotationDegrees: Float
    ) {
        val wheelBitmap = loadBitmap(
            context = context,
            drawableResourceIdentifier = R.drawable.tapp_widget_wheel
        ) ?: return

        canvas.withRotation(
            wheelRotationDegrees,
            wheelRectangle.centerX(),
            wheelRectangle.centerY()
        ) {
            drawBitmap(wheelBitmap, null, wheelRectangle, bitmapPaint)
        }
    }

    private fun drawWheelFrame(
        context: Context,
        canvas: Canvas,
        frameRectangle: RectF
    ) {
        val wheelFrameBitmap = loadBitmap(
            context = context,
            drawableResourceIdentifier = R.drawable.tapp_widget_wheel_frame
        ) ?: return

        canvas.drawBitmap(wheelFrameBitmap, null, frameRectangle, bitmapPaint)
    }

    private fun drawSpinButton(
        context: Context,
        canvas: Canvas,
        frameRectangle: RectF
    ) {
        val spinButtonBitmap = loadBitmap(
            context = context,
            drawableResourceIdentifier = R.drawable.tapp_widget_wheel_spin
        ) ?: return

        val spinButtonWidthPixels = frameRectangle.width() * SPIN_BUTTON_WIDTH_RATIO_OF_FRAME
        val spinButtonHeightPixels = spinButtonWidthPixels *
            spinButtonBitmap.height.toFloat() / spinButtonBitmap.width.toFloat()
        val spinButtonRectangle = createCenteredRectangle(
            centerX = frameRectangle.centerX(),
            centerY = frameRectangle.centerY() + frameRectangle.height() *
                WHEEL_CENTER_VERTICAL_OFFSET_RATIO_OF_FRAME,
            widthPixels = spinButtonWidthPixels,
            heightPixels = spinButtonHeightPixels
        )

        canvas.drawBitmap(spinButtonBitmap, null, spinButtonRectangle, bitmapPaint)
    }

    private fun createCenteredSquare(
        sizePixels: Float,
        centerVerticalOffsetPixels: Float = 0f
    ): RectF {
        return createCenteredRectangle(
            centerX = WHEEL_BITMAP_SIZE_PIXELS / 2f,
            centerY = WHEEL_BITMAP_SIZE_PIXELS / 2f +
                WHEEL_BITMAP_SIZE_PIXELS * CONTENT_CENTER_VERTICAL_OFFSET_RATIO +
                centerVerticalOffsetPixels,
            widthPixels = sizePixels,
            heightPixels = sizePixels
        )
    }

    private fun createCenteredRectangle(
        centerX: Float,
        centerY: Float,
        widthPixels: Float,
        heightPixels: Float
    ): RectF {
        val left = centerX - widthPixels / 2f
        val top = centerY - heightPixels / 2f
        return RectF(left, top, left + widthPixels, top + heightPixels)
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
}
