package com.tapp.sdk.library.widget

import android.content.BroadcastReceiver
import android.os.SystemClock
import com.tapp.sdk.library.domain.TappSpinEasing
import com.tapp.sdk.library.internal.logD
import kotlin.random.Random

internal object TappWidgetAnimationController {

    private const val TARGET_FRAMES_PER_SECOND = 20
    private const val MILLISECONDS_IN_SECOND = 1_000L
    private const val FRAME_DELAY_MILLISECONDS =
        MILLISECONDS_IN_SECOND / TARGET_FRAMES_PER_SECOND
    private const val DEGREES_IN_FULL_SPIN = 360f

    fun startSpinAnimation(
        appWidgetIdentifier: Int,
        animationConfiguration: TappWidgetSpinAnimationConfiguration,
        pendingResult: BroadcastReceiver.PendingResult,
        updateTappWidget: (wheelRotationDegrees: Float) -> Unit
    ) {
        Thread {
            try {
                animateSpin(
                    animationConfiguration = animationConfiguration,
                    updateTappWidget = updateTappWidget
                )
            } finally {
                pendingResult.finish()
            }
        }.apply {
            name = "TappWidgetSpinAnimation-$appWidgetIdentifier"
            start()
        }
    }

    private fun animateSpin(
        animationConfiguration: TappWidgetSpinAnimationConfiguration,
        updateTappWidget: (wheelRotationDegrees: Float) -> Unit
    ) {
        val durationMilliseconds = animationConfiguration.durationMilliseconds
            .coerceAtLeast(FRAME_DELAY_MILLISECONDS)
        val startTimeMilliseconds = SystemClock.uptimeMillis()
        val endTimeMilliseconds = startTimeMilliseconds + durationMilliseconds
        val totalRotationDegrees = calculateTotalRotationDegrees(animationConfiguration)
        val estimatedFrameCount = durationMilliseconds / FRAME_DELAY_MILLISECONDS

        logD(
            "animateSpin: durationMilliseconds=$durationMilliseconds, " +
                "targetFramesPerSecond=$TARGET_FRAMES_PER_SECOND, " +
                "estimatedFrameCount=$estimatedFrameCount"
        )

        while (SystemClock.uptimeMillis() < endTimeMilliseconds) {
            val elapsedMilliseconds = SystemClock.uptimeMillis() - startTimeMilliseconds
            val progress = (elapsedMilliseconds.toFloat() / durationMilliseconds)
                .coerceIn(0f, 1f)
            val easedProgress = calculateEasedProgress(
                progress = progress,
                spinEasing = animationConfiguration.spinEasing
            )
            val wheelRotationDegrees = totalRotationDegrees * easedProgress

            updateTappWidget(wheelRotationDegrees)

            sleepUntilNextFrame(endTimeMilliseconds)
        }

        updateTappWidget(totalRotationDegrees)
    }

    private fun sleepUntilNextFrame(endTimeMilliseconds: Long) {
        val remainingMilliseconds = endTimeMilliseconds - SystemClock.uptimeMillis()
        val sleepMilliseconds = minOf(FRAME_DELAY_MILLISECONDS, remainingMilliseconds)
            .coerceAtLeast(0L)

        if (sleepMilliseconds > 0L) {
            Thread.sleep(sleepMilliseconds)
        }
    }

    private fun calculateTotalRotationDegrees(
        animationConfiguration: TappWidgetSpinAnimationConfiguration
    ): Float {
        val minimumSpins = minOf(
            animationConfiguration.minimumSpins,
            animationConfiguration.maximumSpins
        )
        val maximumSpins = maxOf(
            animationConfiguration.minimumSpins,
            animationConfiguration.maximumSpins
        )
        val spinRange = maximumSpins - minimumSpins
        val spinCount = if (spinRange == 0f) {
            minimumSpins
        } else {
            Random.nextFloat() * spinRange + minimumSpins
        }

        return spinCount * DEGREES_IN_FULL_SPIN
    }

    private fun calculateEasedProgress(
        progress: Float,
        spinEasing: TappSpinEasing
    ): Float {
        return when (spinEasing) {
            TappSpinEasing.EaseInOutCubic -> calculateEaseInOutCubicProgress(progress)
        }
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
