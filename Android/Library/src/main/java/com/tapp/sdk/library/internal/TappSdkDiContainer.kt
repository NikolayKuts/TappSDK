package com.tapp.sdk.library.internal

import com.tapp.sdk.library.data.repository.DefaultTappConfigurationRepository
import com.tapp.sdk.library.domain.repository.ITappConfigurationRepository
import com.tapp.sdk.library.network.ITappConfigurationRemoteDataSource
import com.tapp.sdk.library.network.OkHttpTappConfigurationRemoteDataSource
import com.tapp.sdk.library.storage.ITappConfigurationStorage
import com.tapp.sdk.library.storage.InMemoryTappConfigurationStorage
import okhttp3.OkHttpClient

internal object TappSdkDiContainer {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val configurationRemoteDataSource: ITappConfigurationRemoteDataSource by lazy {
        OkHttpTappConfigurationRemoteDataSource(okHttpClient)
    }

    private val configurationStorage: ITappConfigurationStorage = InMemoryTappConfigurationStorage

    val configurationRepository: ITappConfigurationRepository by lazy {
        DefaultTappConfigurationRepository(
            remoteDataSource = configurationRemoteDataSource,
            configurationStorage = configurationStorage
        )
    }
}
