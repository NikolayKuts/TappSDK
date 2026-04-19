package com.tapp.sdk.library.widget

import com.tapp.sdk.library.storage.ITappAssetStorage

internal object TappWidgetAssetFileResolver {

    fun resolve(
        widgetConfiguration: TappWidgetConfiguration,
        assetStorage: ITappAssetStorage
    ): TappWidgetAssetFiles {
        val assetsConfiguration = widgetConfiguration.assetsConfiguration

        return TappWidgetAssetFiles(
            background = assetStorage.getAssetFile(
                experienceId = widgetConfiguration.identifier,
                fileName = assetsConfiguration.background
            ),
            wheel = assetStorage.getAssetFile(
                experienceId = widgetConfiguration.identifier,
                fileName = assetsConfiguration.wheel
            ),
            wheelFrame = assetStorage.getAssetFile(
                experienceId = widgetConfiguration.identifier,
                fileName = assetsConfiguration.wheelFrame
            ),
            wheelSpin = assetStorage.getAssetFile(
                experienceId = widgetConfiguration.identifier,
                fileName = assetsConfiguration.wheelSpin
            )
        )
    }
}
