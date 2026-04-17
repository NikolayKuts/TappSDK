package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappWheelContract(
    val rotation: TappWheelRotationContract? = null,
    val assets: TappWheelAssetsContract? = null
)
