package com.tapp.sdk.library.data.mapper

import com.tapp.sdk.library.domain.TappConfiguration
import com.tapp.sdk.library.network.TappNetworkRequestConfiguration

internal fun TappConfiguration?.toTappNetworkRequestConfiguration(): TappNetworkRequestConfiguration {
    val networkAttributes = this?.data
        ?.firstNotNullOfOrNull { experience -> experience.network?.attributes }

    return TappNetworkRequestConfiguration(
        timeoutMilliseconds = networkAttributes?.networkTimeout
            ?: TappNetworkRequestConfiguration.Default.timeoutMilliseconds,
        retryAttempts = networkAttributes?.retryAttempts
            ?: TappNetworkRequestConfiguration.Default.retryAttempts
    )
}
