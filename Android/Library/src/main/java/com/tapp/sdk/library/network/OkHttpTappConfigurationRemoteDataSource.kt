package com.tapp.sdk.library.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class OkHttpTappConfigurationRemoteDataSource(
    private val okHttpClient: OkHttpClient
) : ITappConfigurationRemoteDataSource {

    override suspend fun fetchConfigurationJson(
        configurationUrl: String,
        requestConfiguration: TappNetworkRequestConfiguration
    ): String {
        val request = Request.Builder()
            .url(configurationUrl)
            .get()
            .build()

        val configuredOkHttpClient = okHttpClient.withRequestConfiguration(requestConfiguration)

        return requestConfiguration.executeWithRetry("fetch configuration") {
            configuredOkHttpClient.newCall(request).awaitString()
        }
    }

    private suspend fun Call.awaitString(): String = suspendCancellableCoroutine { continuation ->
        enqueue(
            object : Callback {

                override fun onFailure(call: Call, exception: IOException) {
                    if (!continuation.isCancelled) {
                        continuation.resumeWithException(exception)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            continuation.resumeWithException(
                                TappHttpException(
                                    statusCode = response.code,
                                    message = "Failed to fetch configuration: ${response.code}"
                                )
                            )
                            return
                        }

                        val responseBody = response.body?.string()
                        if (responseBody == null) {
                            continuation.resumeWithException(
                                IOException("Configuration response body is empty")
                            )
                            return
                        }

                        continuation.resume(responseBody)
                    }
                }
            }
        )

        continuation.invokeOnCancellation {
            cancel()
        }
    }
}
