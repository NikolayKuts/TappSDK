package com.tapp.sdk.library.internal

import android.content.Context
import com.tapp.sdk.library.data.repository.DefaultTappConfigurationRepository
import com.tapp.sdk.library.domain.repository.ITappConfigurationRepository
import com.tapp.sdk.library.network.ITappAssetRemoteDataSource
import com.tapp.sdk.library.network.ITappConfigurationRemoteDataSource
import com.tapp.sdk.library.network.OkHttpTappAssetRemoteDataSource
import com.tapp.sdk.library.network.OkHttpTappConfigurationRemoteDataSource
import com.tapp.sdk.library.storage.ITappWidgetConfigurationStorage
import com.tapp.sdk.library.storage.ITappConfigurationStorage
import com.tapp.sdk.library.storage.InMemoryTappWidgetConfigurationStorage
import com.tapp.sdk.library.storage.InMemoryTappConfigurationStorage
import okhttp3.OkHttpClient

internal object TappSdkDiContainer {

    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val configurationRemoteDataSource: ITappConfigurationRemoteDataSource by lazy {
        OkHttpTappConfigurationRemoteDataSource(okHttpClient)
    }

    val assetRemoteDataSource: ITappAssetRemoteDataSource by lazy {
        OkHttpTappAssetRemoteDataSource(okHttpClient)
    }

    private val configurationStorage: ITappConfigurationStorage = InMemoryTappConfigurationStorage

    val widgetConfigurationStorage: ITappWidgetConfigurationStorage =
        InMemoryTappWidgetConfigurationStorage

    val configurationRepository: ITappConfigurationRepository by lazy {
        DefaultTappConfigurationRepository(
            remoteDataSource = configurationRemoteDataSource,
            configurationStorage = configurationStorage
        )
    }
}
