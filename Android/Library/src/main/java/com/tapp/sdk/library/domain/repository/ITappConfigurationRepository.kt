package com.tapp.sdk.library.domain.repository

import com.tapp.sdk.library.domain.TappConfiguration

interface ITappConfigurationRepository {

    suspend fun fetchConfiguration(configurationUrl: String): TappConfiguration

    suspend fun getCachedConfiguration(): TappConfiguration?
}
