package com.tapp.sdk.library.domain

data class TappConfiguration(
    val data: List<TappExperience> = emptyList(),
    val meta: TappMeta? = null
)
