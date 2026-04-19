package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappNetworkAttributes(
    val refreshInterval: Long,
    val networkTimeout: Long,
    val retryAttempts: Int,
    val cacheExpiration: Long,
    val debugMode: Boolean
)
