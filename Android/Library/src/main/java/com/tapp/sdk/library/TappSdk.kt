package com.tapp.sdk.library

import android.content.Context
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
        }
    }
}
