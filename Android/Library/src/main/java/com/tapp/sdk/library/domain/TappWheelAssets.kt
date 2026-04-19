package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappWheelAssets(
    val background: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String
)
