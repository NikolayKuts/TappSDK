package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappWheelRotationContract(
    val duration: Long? = null,
    val minimumSpins: Float? = null,
    val maximumSpins: Float? = null,
    val spinEasing: TappSpinEasingContract? = null
)
