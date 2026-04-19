package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappMetaContract(
    val version: Int? = null,
    val copyright: String? = null
)
