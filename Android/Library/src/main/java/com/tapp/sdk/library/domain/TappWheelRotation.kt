package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappWheelRotation(
    val duration: Long,
    val minimumSpins: Float,
    val maximumSpins: Float,
    val spinEasing: TappSpinEasing
)
