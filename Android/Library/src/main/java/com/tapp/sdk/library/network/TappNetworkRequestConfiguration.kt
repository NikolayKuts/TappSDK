package com.tapp.sdk.library.network

import com.tapp.sdk.library.internal.logD
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.OkHttpClient

private const val DEFAULT_TIMEOUT_MILLISECONDS = 30_000L
private const val DEFAULT_RETRY_ATTEMPTS = 3
private const val MINIMUM_TIMEOUT_MILLISECONDS = 1L

internal data class TappNetworkRequestConfiguration(
    val timeoutMilliseconds: Long = DEFAULT_TIMEOUT_MILLISECONDS,
    val retryAttempts: Int = DEFAULT_RETRY_ATTEMPTS
) {

    val safeTimeoutMilliseconds: Long
        get() = timeoutMilliseconds.coerceAtLeast(MINIMUM_TIMEOUT_MILLISECONDS)

    val safeRetryAttempts: Int
        get() = retryAttempts.coerceAtLeast(0)

    companion object {

        val Default = TappNetworkRequestConfiguration()
    }
}

internal class TappHttpException(
    val statusCode: Int,
    message: String
) : IOException(message)

internal fun OkHttpClient.withRequestConfiguration(
    requestConfiguration: TappNetworkRequestConfiguration
): OkHttpClient {
    return newBuilder()
        .callTimeout(requestConfiguration.safeTimeoutMilliseconds, TimeUnit.MILLISECONDS)
        .connectTimeout(requestConfiguration.safeTimeoutMilliseconds, TimeUnit.MILLISECONDS)
        .readTimeout(requestConfiguration.safeTimeoutMilliseconds, TimeUnit.MILLISECONDS)
        .writeTimeout(requestConfiguration.safeTimeoutMilliseconds, TimeUnit.MILLISECONDS)
        .build()
}

internal suspend fun <T> TappNetworkRequestConfiguration.executeWithRetry(
    operationName: String,
    block: suspend () -> T
): T {
    val maxAttempts = safeRetryAttempts + 1
    var attempt = 1
    var lastThrowable: Throwable? = null

    while (attempt <= maxAttempts) {
        try {
            return block()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            lastThrowable = throwable

            if (attempt == maxAttempts || !throwable.isRetryable()) {
                throw throwable
            }

            logD(
                message = "$operationName failed, retrying: attempt=$attempt, maxAttempts=$maxAttempts",
                throwable = throwable
            )
            attempt++
        }
    }

    throw lastThrowable ?: IOException("$operationName failed")
}

private fun Throwable.isRetryable(): Boolean {
    return when (this) {
        is TappHttpException -> statusCode == 408 ||
            statusCode == 429 ||
            statusCode in 500..599
        is IOException -> true
        else -> false
    }
}
