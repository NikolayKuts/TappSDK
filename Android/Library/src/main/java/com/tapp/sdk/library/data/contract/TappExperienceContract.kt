package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappExperienceContract(
    val id: String? = null,
    val name: String? = null,
    val type: TappSurfaceTypeContract? = null,
    val network: TappNetworkContract? = null,
    val wheel: TappWheelContract? = null
)
