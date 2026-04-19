package com.tapp.sdk.library.data.repository

import com.tapp.sdk.library.data.mapper.toDomain
import com.tapp.sdk.library.data.serialization.TappJsonConfigurationDecoder
import com.tapp.sdk.library.domain.TappConfiguration
import com.tapp.sdk.library.domain.repository.ITappConfigurationRepository
import com.tapp.sdk.library.network.ITappConfigurationRemoteDataSource
import com.tapp.sdk.library.storage.ITappConfigurationLocalStorage

internal class DefaultTappConfigurationRepository(
    private val remoteDataSource: ITappConfigurationRemoteDataSource,
    private val configurationLocalStorage: ITappConfigurationLocalStorage,
    private val jsonConfigurationDecoder: TappJsonConfigurationDecoder = TappJsonConfigurationDecoder
) : ITappConfigurationRepository {

    override suspend fun fetchConfiguration(configurationUrl: String): TappConfiguration {
        val configurationJson = remoteDataSource.fetchConfigurationJson(configurationUrl)
        val configuration = jsonConfigurationDecoder
            .decode(configurationJson)
            .toDomain()

        configurationLocalStorage.cachedConfiguration = configuration
        configurationLocalStorage.configurationFetchedAtMillis = System.currentTimeMillis()

        return configuration
    }

    override suspend fun getCachedConfiguration(): TappConfiguration? {
        return configurationLocalStorage.cachedConfiguration
    }
}
