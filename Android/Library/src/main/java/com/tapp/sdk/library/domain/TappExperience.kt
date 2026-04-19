package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappExperience(
    val id: String,
    val name: String,
    val surfaceType: TappSurfaceType,
    val contentViewType: TappContentViewType,
    val network: TappNetwork? = null,
    val wheel: TappWheel? = null
)
