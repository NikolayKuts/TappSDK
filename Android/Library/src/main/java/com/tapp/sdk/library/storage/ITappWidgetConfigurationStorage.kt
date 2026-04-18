package com.tapp.sdk.library.storage

import com.tapp.sdk.library.widget.TappWidgetConfiguration

internal interface ITappWidgetConfigurationStorage {

    fun saveAvailableWidgetConfigurations(widgetConfigurations: List<TappWidgetConfiguration>)

    fun getWidgetConfiguration(appWidgetIdentifier: Int): TappWidgetConfiguration

    fun assignWidgetConfiguration(
        appWidgetIdentifier: Int,
        widgetConfigurationIdentifier: String
    ): Boolean

    fun removeWidgetConfiguration(appWidgetIdentifier: Int)
}
