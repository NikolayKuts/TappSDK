package com.tapp.sdk.library.domain

data class TappExperience(
    val id: String,
    val name: String,
    val surfaceType: TappSurfaceType,
    val contentViewType: TappContentViewType,
    val network: TappNetwork? = null,
    val wheel: TappWheel? = null
)
