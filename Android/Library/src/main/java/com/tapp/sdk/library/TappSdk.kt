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
                .getOrNull()
                ?: runCatching {
                    TappSdkDiContainer.configurationRepository.getCachedConfiguration()
                }.onFailure {
                    logD("failed to read cached config", it)
                }.getOrNull()
                    ?.also { cachedConfiguration ->
                        logD("using cached config: $cachedConfiguration")
                    }
                ?: run {
                    logD("configuration unavailable")
                    return@launch
                }

            logD("initialize: $configuration")

            TappSdkDiContainer.widgetConfigurationStorage.saveAvailableWidgetConfigurations(
                configuration.toTappWidgetConfigurations()
            )

            downloadConfigurationAssets(configuration)
        }
    }

    private suspend fun downloadConfigurationAssets(configuration: TappConfiguration) {
        val assets = configuration.toTappAssets()

        if (assets.isEmpty()) {
            logD("no remote assets to download")
            return
        }

        assets.forEach { asset ->
            runCatching {
                val assetBytes = TappSdkDiContainer.assetRemoteDataSource.fetchAssetBytes(asset.url)
                TappSdkDiContainer.assetStorage.saveAsset(
                    experienceId = asset.experienceId,
                    fileName = asset.fileName,
                    bytes = assetBytes
                ) to assetBytes.size
            }.onSuccess { (assetFile, assetSizeBytes) ->
                logD(
                    "asset saved: " +
                        "fileName=${asset.fileName}, " +
                        "sizeBytes=$assetSizeBytes, " +
                        "localPath=${assetFile.absolutePath}"
                )
            }.onFailure { throwable ->
                logD("failed to download asset: url=${asset.url}", throwable)
            }
        }
    }

    private fun TappConfiguration.toTappAssets(): List<TappAsset> {
        val assets = mutableListOf<TappAsset>()

        data.forEach { experience ->
            if (
                experience.surfaceType != TappSurfaceType.Widget ||
                experience.contentViewType != TappContentViewType.Wheel
            ) {
                return@forEach
            }

            val assetHost = experience.network?.assets?.host ?: return@forEach
            val wheelAssets = experience.wheel?.assets ?: return@forEach

            assets += listOf(
                TappAsset(
                    experienceId = experience.id,
                    fileName = wheelAssets.background,
                    url = assetHost + wheelAssets.background
                ),
                TappAsset(
                    experienceId = experience.id,
                    fileName = wheelAssets.wheelFrame,
                    url = assetHost + wheelAssets.wheelFrame
                ),
                TappAsset(
                    experienceId = experience.id,
                    fileName = wheelAssets.wheelSpin,
                    url = assetHost + wheelAssets.wheelSpin
                ),
                TappAsset(
                    experienceId = experience.id,
                    fileName = wheelAssets.wheel,
                    url = assetHost + wheelAssets.wheel
                )
            )
        }

        return assets
    }

    private data class TappAsset(
        val experienceId: String,
        val fileName: String,
        val url: String
    )
}
