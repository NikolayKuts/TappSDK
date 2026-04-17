package com.tapp.sdk.library.domain

data class TappNetworkAttributes(
    val refreshInterval: Long,
    val networkTimeout: Long,
    val retryAttempts: Int,
    val cacheExpiration: Long,
    val debugMode: Boolean
)
