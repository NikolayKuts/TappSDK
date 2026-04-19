package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappConfiguration(
    val data: List<TappExperience> = emptyList(),
    val meta: TappMeta? = null
)
