package com.tapp.sdk.library.storage

import com.tapp.sdk.library.domain.TappConfiguration

internal interface ITappConfigurationStorage {

    suspend fun saveConfiguration(configuration: TappConfiguration)

    suspend fun getConfiguration(): TappConfiguration?

    suspend fun saveLastFetchTimeMillis(lastFetchTimeMillis: Long)

    suspend fun getLastFetchTimeMillis(): Long?
}
