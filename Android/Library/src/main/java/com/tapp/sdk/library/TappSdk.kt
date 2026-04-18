package com.tapp.sdk.library

import android.content.Context
import com.tapp.sdk.library.domain.TappConfiguration
import com.tapp.sdk.library.domain.TappContentViewType
import com.tapp.sdk.library.domain.TappSurfaceType
import com.tapp.sdk.library.internal.TappSdkDiContainer
import com.tapp.sdk.library.internal.logD
import com.tapp.sdk.library.widget.toTappWidgetConfigurations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object TappSdk {

    private val sdkCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(
        context: Context,
        configurationUrl: String
    ) {
        TappSdkDiContainer.initialize(context.applicationContext)

        sdkCoroutineScope.launch {
            val configuration = runCatching {
                val config = TappSdkDiContainer.configurationRepository.fetchConfiguration(configurationUrl)
                logD("fetched config: $config")
                config
            }.onFailure {
                logD("failed to fetch config", it)
            }
                .getOrNull() ?: return@launch

            logD("initialize: $configuration")

            TappSdkDiContainer.widgetConfigurationStorage.saveAvailableWidgetConfigurations(
                configuration.toTappWidgetConfigurations()
            )

            downloadConfigurationAssets(configuration)
        }
    }

    private suspend fun downloadConfigurationAssets(configuration: TappConfiguration) {
        val assetUrls = configuration.toTappAssetUrls()

        if (assetUrls.isEmpty()) {
            logD("no remote assets to download")
            return
        }

        assetUrls.forEach { assetUrl ->
            runCatching {
                TappSdkDiContainer.assetRemoteDataSource.fetchAssetBytes(assetUrl)
            }.onSuccess { assetBytes ->
                logD("asset downloaded: sizeBytes=${assetBytes.size}, url=$assetUrl")
            }.onFailure { throwable ->
                logD("failed to download asset: url=$assetUrl", throwable)
            }
        }
    }

    private fun TappConfiguration.toTappAssetUrls(): List<String> {
        val assetUrls = mutableListOf<String>()

        data.forEach { experience ->
            if (
                experience.surfaceType != TappSurfaceType.Widget ||
                experience.contentViewType != TappContentViewType.Wheel
            ) {
                return@forEach
            }

            val assetHost = experience.network?.assets?.host ?: return@forEach
            val wheelAssets = experience.wheel?.assets ?: return@forEach

            assetUrls += listOf(
                assetHost + wheelAssets.background,
                assetHost + wheelAssets.wheelFrame,
                assetHost + wheelAssets.wheelSpin,
                assetHost + wheelAssets.wheel
            )
        }

        return assetUrls
    }
}
