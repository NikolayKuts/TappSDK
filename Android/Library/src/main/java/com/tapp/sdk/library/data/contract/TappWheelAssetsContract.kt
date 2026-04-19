package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappWheelAssetsContract(
    val bg: String? = null,
    val wheelFrame: String? = null,
    val wheelSpin: String? = null,
    val wheel: String? = null
)
