package com.tapp.sdk.library.widget

import android.content.BroadcastReceiver
import android.os.SystemClock
import com.tapp.sdk.library.domain.TappSpinEasing
import com.tapp.sdk.library.internal.logD
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object TappWidgetAnimationController {

    private const val TARGET_FRAMES_PER_SECOND = 20
    private const val MILLISECONDS_IN_SECOND = 1_000L
    private const val FRAME_DELAY_MILLISECONDS =
        MILLISECONDS_IN_SECOND / TARGET_FRAMES_PER_SECOND
    private const val DEGREES_IN_FULL_SPIN = 360f
    private val animationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val animationJobsLock = Any()
    private val animationJobsByAppWidgetIdentifier = mutableMapOf<Int, Job>()

    fun startSpinAnimation(
        appWidgetIdentifier: Int,
        animationConfiguration: TappWidgetSpinAnimationConfiguration,
        pendingResult: BroadcastReceiver.PendingResult,
        updateTappWidget: (wheelRotationDegrees: Float) -> Unit
    ) {
        var previousAnimationJob: Job? = null

        val currentAnimationJob = animationCoroutineScope.launch(start = CoroutineStart.LAZY) {
            try {
                previousAnimationJob.cancelIfActive()
                animateSpin(
                    animationConfiguration = animationConfiguration,
                    updateTappWidget = updateTappWidget
                )
            } finally {
                pendingResult.finish()
                removeAnimationJob(
                    appWidgetIdentifier = appWidgetIdentifier,
                    animationJob = coroutineContext[Job]
                )
            }
        }

        previousAnimationJob = replaceAnimationJob(
            appWidgetIdentifier = appWidgetIdentifier,
            animationJob = currentAnimationJob
        )
        currentAnimationJob.start()
    }

    private fun replaceAnimationJob(
        appWidgetIdentifier: Int,
        animationJob: Job
    ): Job? {
        return synchronized(animationJobsLock) {
            animationJobsByAppWidgetIdentifier.put(appWidgetIdentifier, animationJob)
        }
    }

    private suspend fun Job?.cancelIfActive() {
        this?.cancelAndJoin()
    }

    private fun removeAnimationJob(
        appWidgetIdentifier: Int,
        animationJob: Job?
    ) {
        synchronized(animationJobsLock) {
            if (animationJobsByAppWidgetIdentifier[appWidgetIdentifier] === animationJob) {
                animationJobsByAppWidgetIdentifier.remove(appWidgetIdentifier)
            }
        }
    }

    private suspend fun animateSpin(
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

            delayUntilNextFrame(endTimeMilliseconds)
        }

        updateTappWidget(totalRotationDegrees)
    }

    private suspend fun delayUntilNextFrame(endTimeMilliseconds: Long) {
        val remainingMilliseconds = endTimeMilliseconds - SystemClock.uptimeMillis()
        val delayMilliseconds = minOf(FRAME_DELAY_MILLISECONDS, remainingMilliseconds)
            .coerceAtLeast(0L)

        if (delayMilliseconds > 0L) {
            delay(delayMilliseconds)
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
