package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappNetworkContract(
    val attributes: TappNetworkAttributesContract? = null,
    val assets: TappNetworkAssetsContract? = null
)
