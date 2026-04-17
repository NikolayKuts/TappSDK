package com.tapp.sdk.library.widget

import android.content.BroadcastReceiver
import kotlin.random.Random

internal object TappWidgetAnimationController {

    private const val FRAME_COUNT = 60
    private const val ANIMATION_DURATION_MILLISECONDS = 2_000L
    private const val FRAME_DELAY_MILLISECONDS = ANIMATION_DURATION_MILLISECONDS / FRAME_COUNT
    private const val DEGREES_IN_FULL_SPIN = 360f
    private const val MINIMUM_SPINS = 3f
    private const val MAXIMUM_SPINS = 5f

    fun startSpinAnimation(
        appWidgetIdentifier: Int,
        pendingResult: BroadcastReceiver.PendingResult,
        updateTappWidget: (wheelRotationDegrees: Float) -> Unit
    ) {
        Thread {
            try {
                animateSpin(updateTappWidget)
            } finally {
                pendingResult.finish()
            }
        }.apply {
            name = "TappWidgetSpinAnimation-$appWidgetIdentifier"
            start()
        }
    }

    private fun animateSpin(
        updateTappWidget: (wheelRotationDegrees: Float) -> Unit
    ) {
        val totalRotationDegrees = calculateTotalRotationDegrees()

        repeat(FRAME_COUNT) { frameIndex ->
            val progress = (frameIndex + 1).toFloat() / FRAME_COUNT
            val easedProgress = calculateEaseInOutCubicProgress(progress)
            val wheelRotationDegrees = totalRotationDegrees * easedProgress

            updateTappWidget(wheelRotationDegrees)

            if (frameIndex < FRAME_COUNT - 1) {
                Thread.sleep(FRAME_DELAY_MILLISECONDS)
            }
        }
    }

    private fun calculateTotalRotationDegrees(): Float {
        val spinCount = Random.nextFloat() *
            (MAXIMUM_SPINS - MINIMUM_SPINS) + MINIMUM_SPINS

        return spinCount * DEGREES_IN_FULL_SPIN
    }

    private fun calculateEaseInOutCubicProgress(progress: Float): Float {
        return if (progress < 0.5f) {
            4f * progress * progress * progress
        } else {
            val adjustedProgress = -2f * progress + 2f
            1f - adjustedProgress * adjustedProgress * adjustedProgress / 2f
        }
    }
}
