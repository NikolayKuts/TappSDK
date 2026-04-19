package com.tapp.sdk.library.domain

import kotlinx.serialization.Serializable

@Serializable
data class TappMeta(
    val version: Int,
    val copyright: String
)
