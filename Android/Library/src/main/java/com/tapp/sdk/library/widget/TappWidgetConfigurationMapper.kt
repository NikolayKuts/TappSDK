package com.tapp.sdk.library.widget

import com.tapp.sdk.library.domain.TappConfiguration
import com.tapp.sdk.library.domain.TappContentViewType
import com.tapp.sdk.library.domain.TappSurfaceType

internal fun TappConfiguration.toTappWidgetConfigurations(): List<TappWidgetConfiguration> {
    return data.mapNotNull { experience ->
        val wheel = experience.wheel ?: return@mapNotNull null

        if (
            experience.surfaceType != TappSurfaceType.Widget ||
            experience.contentViewType != TappContentViewType.Wheel
        ) {
            return@mapNotNull null
        }

        TappWidgetConfiguration(
            identifier = experience.id,
            name = experience.name,
            animationConfiguration = TappWidgetSpinAnimationConfiguration(
                durationMilliseconds = wheel.rotation.duration,
                minimumSpins = wheel.rotation.minimumSpins,
                maximumSpins = wheel.rotation.maximumSpins,
                spinEasing = wheel.rotation.spinEasing
            ),
            assetsConfiguration = TappWidgetAssetsConfiguration(
                background = wheel.assets.background,
                wheelFrame = wheel.assets.wheelFrame,
                wheelSpin = wheel.assets.wheelSpin,
                wheel = wheel.assets.wheel
            )
        )
    }
}
