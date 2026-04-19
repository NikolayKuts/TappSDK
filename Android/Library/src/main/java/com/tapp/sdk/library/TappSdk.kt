package com.tapp.sdk.library

import android.content.Context
import com.tapp.sdk.library.data.mapper.toTappNetworkRequestConfiguration
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

    private const val DEFAULT_REFRESH_INTERVAL_SECONDS = 300L
    private const val DEFAULT_CACHE_EXPIRATION_SECONDS = 3_600L
    private const val MILLISECONDS_IN_SECOND = 1_000L

    private val sdkCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(
        context: Context,
        configurationUrl: String
    ) {
        TappSdkDiContainer.initialize(context.applicationContext)

        sdkCoroutineScope.launch {
            val initializationResult = loadConfiguration(configurationUrl)

            if (initializationResult == null) {
                logD("configuration unavailable, using default widget configuration")
                TappSdkDiContainer.widgetConfigurationStorage.saveAvailableWidgetConfigurations(emptyList())
                return@launch
            }

            val configuration = initializationResult.configuration

            logD("initialize: $configuration")

            TappSdkDiContainer.widgetConfigurationStorage.saveAvailableWidgetConfigurations(
                configuration.toTappWidgetConfigurations()
            )

            if (initializationResult.shouldDownloadAssets) {
                downloadConfigurationAssets(configuration)
            }
        }
    }

    private suspend fun loadConfiguration(
        configurationUrl: String
    ): TappConfigurationInitializationResult? {
        val cachedConfiguration = readCachedConfiguration()
        val configurationFetchedAtMillis = readConfigurationFetchedAtMillis()
        val currentTimeMillis = System.currentTimeMillis()

        if (
            cachedConfiguration != null &&
            configurationFetchedAtMillis != null &&
            cachedConfiguration.isRefreshIntervalActive(
                configurationFetchedAtMillis = configurationFetchedAtMillis,
                currentTimeMillis = currentTimeMillis
            )
        ) {
            logD("using cached config: refresh interval is active")
            return TappConfigurationInitializationResult(
                configuration = cachedConfiguration,
                shouldDownloadAssets = false
            )
        }

        val remoteConfiguration = runCatching {
            TappSdkDiContainer.configurationRepository.fetchConfiguration(configurationUrl)
        }.onSuccess { configuration ->
            logD("fetched config: $configuration")
        }.onFailure { throwable ->
            logD("failed to fetch config", throwable)
        }.getOrNull()

        if (remoteConfiguration != null) {
            return TappConfigurationInitializationResult(
                configuration = remoteConfiguration,
                shouldDownloadAssets = true
            )
        }

        if (
            cachedConfiguration != null &&
            configurationFetchedAtMillis != null &&
            !cachedConfiguration.isCacheExpired(
                configurationFetchedAtMillis = configurationFetchedAtMillis,
                currentTimeMillis = currentTimeMillis
            )
        ) {
            logD("using cached config after failed fetch: $cachedConfiguration")
            return TappConfigurationInitializationResult(
                configuration = cachedConfiguration,
                shouldDownloadAssets = false
            )
        }

        if (cachedConfiguration != null) {
            logD("cached config expired")
        }

        return null
    }

    private suspend fun readCachedConfiguration(): TappConfiguration? {
        return runCatching {
            TappSdkDiContainer.configurationRepository.getCachedConfiguration()
        }.onFailure { throwable ->
            logD("failed to read cached config", throwable)
        }.getOrNull()
    }

    private suspend fun readConfigurationFetchedAtMillis(): Long? {
        return runCatching {
            TappSdkDiContainer.configurationRepository.getConfigurationFetchedAtMillis()
        }.onFailure { throwable ->
            logD("failed to read configuration fetch time", throwable)
        }.getOrNull()
    }

    private suspend fun downloadConfigurationAssets(configuration: TappConfiguration) {
        val assets = configuration.toTappAssets()
        val requestConfiguration = configuration.toTappNetworkRequestConfiguration()

        if (assets.isEmpty()) {
            logD("no remote assets to download")
            return
        }

        assets.forEach { asset ->
            runCatching {
                val assetBytes = TappSdkDiContainer.assetRemoteDataSource.fetchAssetBytes(
                    assetUrl = asset.url,
                    requestConfiguration = requestConfiguration
                )
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

    private data class TappConfigurationInitializationResult(
        val configuration: TappConfiguration,
        val shouldDownloadAssets: Boolean
    )

    private fun TappConfiguration.isRefreshIntervalActive(
        configurationFetchedAtMillis: Long,
        currentTimeMillis: Long
    ): Boolean {
        return currentTimeMillis - configurationFetchedAtMillis <
            refreshIntervalMilliseconds()
    }

    private fun TappConfiguration.isCacheExpired(
        configurationFetchedAtMillis: Long,
        currentTimeMillis: Long
    ): Boolean {
        return currentTimeMillis - configurationFetchedAtMillis >
            cacheExpirationMilliseconds()
    }

    private fun TappConfiguration.refreshIntervalMilliseconds(): Long {
        return networkRefreshIntervalSeconds().secondsToMilliseconds()
    }

    private fun TappConfiguration.cacheExpirationMilliseconds(): Long {
        return networkCacheExpirationSeconds().secondsToMilliseconds()
    }

    private fun TappConfiguration.networkRefreshIntervalSeconds(): Long {
        return firstNetworkAttributes()?.refreshInterval
            ?: DEFAULT_REFRESH_INTERVAL_SECONDS
    }

    private fun TappConfiguration.networkCacheExpirationSeconds(): Long {
        return firstNetworkAttributes()?.cacheExpiration
            ?: DEFAULT_CACHE_EXPIRATION_SECONDS
    }

    private fun TappConfiguration.firstNetworkAttributes() = data
        .firstNotNullOfOrNull { experience -> experience.network?.attributes }

    private fun Long.secondsToMilliseconds(): Long {
        return coerceAtLeast(0L) * MILLISECONDS_IN_SECOND
    }
}
