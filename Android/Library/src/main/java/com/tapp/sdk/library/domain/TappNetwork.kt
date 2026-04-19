package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappNetwork(
    val attributes: TappNetworkAttributes? = null,
    val assets: TappNetworkAssets? = null
)
