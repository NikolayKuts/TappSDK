package com.tapp.sdk.library.widget

import com.tapp.sdk.library.domain.TappSpinEasing

internal object TappWidgetDefaultConfiguration {

    val configuration = TappWidgetConfiguration(
        identifier = "default_wheel",
        name = "Default Wheel Widget Configuration",
        animationConfiguration = TappWidgetSpinAnimationConfiguration(
            durationMilliseconds = 2_000L,
            minimumSpins = 3f,
            maximumSpins = 5f,
            spinEasing = TappSpinEasing.EaseInOutCubic
        ),
        assetsConfiguration = TappWidgetAssetsConfiguration(
            background = "bg.jpeg",
            wheelFrame = "wheel-frame.png",
            wheelSpin = "wheel-spin.png",
            wheel = "wheel.png"
        )
    )
}
