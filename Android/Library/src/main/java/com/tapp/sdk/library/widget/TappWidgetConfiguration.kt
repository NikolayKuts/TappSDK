package com.tapp.sdk.library.widget

internal data class TappWidgetConfiguration(
    val identifier: String,
    val name: String,
    val animationConfiguration: TappWidgetSpinAnimationConfiguration,
    val assetsConfiguration: TappWidgetAssetsConfiguration
)
