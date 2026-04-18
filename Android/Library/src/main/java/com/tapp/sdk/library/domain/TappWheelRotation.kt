package com.tapp.sdk.library.domain

data class TappWheelRotation(
    val duration: Long,
    val minimumSpins: Float,
    val maximumSpins: Float,
    val spinEasing: TappSpinEasing
)
