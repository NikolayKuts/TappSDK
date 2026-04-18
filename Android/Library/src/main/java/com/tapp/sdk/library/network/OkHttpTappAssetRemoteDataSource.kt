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

internal class OkHttpTappAssetRemoteDataSource(
    private val okHttpClient: OkHttpClient
) : ITappAssetRemoteDataSource {

    override suspend fun fetchAssetBytes(assetUrl: String): ByteArray {
        val request = Request.Builder()
            .url(assetUrl)
            .get()
            .build()

        return okHttpClient.newCall(request).awaitByteArray()
    }

    private suspend fun Call.awaitByteArray(): ByteArray = suspendCancellableCoroutine { continuation ->
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
                                IOException("Failed to fetch asset: ${response.code}")
                            )
                            return
                        }

                        val responseBody = response.body
                        if (responseBody == null) {
                            continuation.resumeWithException(
                                IOException("Asset response body is empty")
                            )
                            return
                        }

                        continuation.resume(responseBody.bytes())
                    }
                }
            }
        )

        continuation.invokeOnCancellation {
            cancel()
        }
    }
}
