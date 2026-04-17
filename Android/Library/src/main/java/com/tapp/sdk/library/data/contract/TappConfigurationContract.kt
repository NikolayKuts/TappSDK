package com.tapp.sdk.library.data.contract

import kotlinx.serialization.Serializable

@Serializable
data class TappConfigurationContract(
    val data: List<TappExperienceContract> = emptyList(),
    val meta: TappMetaContract? = null
)
