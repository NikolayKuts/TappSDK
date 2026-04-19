package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappNetworkAttributesContract(
    val refreshInterval: Long? = null,
    val networkTimeout: Long? = null,
    val retryAttempts: Int? = null,
    val cacheExpiration: Long? = null,
    val debugMode: Boolean? = null
)
