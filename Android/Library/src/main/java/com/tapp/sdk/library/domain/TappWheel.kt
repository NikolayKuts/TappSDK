package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappWheel(
    val rotation: TappWheelRotation,
    val assets: TappWheelAssets
)
