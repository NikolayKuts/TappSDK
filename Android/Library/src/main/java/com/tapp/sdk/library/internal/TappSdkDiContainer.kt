package com.tapp.sdk.library.internal

import android.content.Context
import com.tapp.sdk.library.data.repository.DefaultTappConfigurationRepository
import com.tapp.sdk.library.domain.repository.ITappConfigurationRepository
import com.tapp.sdk.library.network.ITappAssetRemoteDataSource
import com.tapp.sdk.library.network.ITappConfigurationRemoteDataSource
import com.tapp.sdk.library.network.OkHttpTappAssetRemoteDataSource
import com.tapp.sdk.library.network.OkHttpTappConfigurationRemoteDataSource
import com.tapp.sdk.library.storage.FileTappAssetStorage
import com.tapp.sdk.library.storage.ITappAssetStorage
import com.tapp.sdk.library.storage.ITappConfigurationLocalStorage
import com.tapp.sdk.library.storage.ITappWidgetConfigurationStorage
import com.tapp.sdk.library.storage.InMemoryTappWidgetConfigurationStorage
import com.tapp.sdk.library.storage.SharedPreferencesTappConfigurationLocalStorage
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

    val assetStorage: ITappAssetStorage by lazy {
        FileTappAssetStorage(requireNotNull(applicationContext))
    }

    private val configurationLocalStorage: ITappConfigurationLocalStorage by lazy {
        SharedPreferencesTappConfigurationLocalStorage(requireNotNull(applicationContext))
    }

    val widgetConfigurationStorage: ITappWidgetConfigurationStorage =
        InMemoryTappWidgetConfigurationStorage

    val configurationRepository: ITappConfigurationRepository by lazy {
        DefaultTappConfigurationRepository(
            remoteDataSource = configurationRemoteDataSource,
            configurationLocalStorage = configurationLocalStorage
        )
    }
}
