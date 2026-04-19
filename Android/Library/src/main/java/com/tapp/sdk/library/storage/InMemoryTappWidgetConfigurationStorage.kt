package com.tapp.sdk.library.storage

import com.tapp.sdk.library.internal.logD
import com.tapp.sdk.library.widget.TappWidgetConfiguration
import com.tapp.sdk.library.widget.TappWidgetDefaultConfiguration

internal object InMemoryTappWidgetConfigurationStorage : ITappWidgetConfigurationStorage {

    private val lock = Any()
    private val defaultWidgetConfiguration = TappWidgetDefaultConfiguration.configuration
    private var availableWidgetConfigurations = mapOf(
        defaultWidgetConfiguration.identifier to defaultWidgetConfiguration
    )
    private var remoteWidgetConfigurationIdentifiers = emptyList<String>()
    private val widgetConfigurationIdentifiersByAppWidgetIdentifier = mutableMapOf<Int, String>()

    override fun saveAvailableWidgetConfigurations(widgetConfigurations: List<TappWidgetConfiguration>) {
        synchronized(lock) {
            logD("saveAvailableWidgetConfigurations: $widgetConfigurations")
            remoteWidgetConfigurationIdentifiers = widgetConfigurations.map { widgetConfiguration ->
                widgetConfiguration.identifier
            }
            availableWidgetConfigurations = buildMap {
                put(defaultWidgetConfiguration.identifier, defaultWidgetConfiguration)
                widgetConfigurations.forEach { widgetConfiguration ->
                    put(widgetConfiguration.identifier, widgetConfiguration)
                }
            }
        }
    }

    override fun getWidgetConfiguration(appWidgetIdentifier: Int): TappWidgetConfiguration {
        return synchronized(lock) {
            val widgetConfigurationIdentifier =
                widgetConfigurationIdentifiersByAppWidgetIdentifier[appWidgetIdentifier]
            val widgetConfiguration = widgetConfigurationIdentifier
                ?.let(availableWidgetConfigurations::get)

            if (widgetConfiguration != null) {
                return@synchronized widgetConfiguration
            }

            assignFirstAvailableWidgetConfiguration(appWidgetIdentifier)
                ?: defaultWidgetConfiguration
        }
    }

    override fun assignWidgetConfiguration(
        appWidgetIdentifier: Int,
        widgetConfigurationIdentifier: String
    ): Boolean {
        return synchronized(lock) {
            if (!availableWidgetConfigurations.containsKey(widgetConfigurationIdentifier)) {
                return@synchronized false
            }

            widgetConfigurationIdentifiersByAppWidgetIdentifier[appWidgetIdentifier] =
                widgetConfigurationIdentifier
            true
        }
    }

    override fun removeWidgetConfiguration(appWidgetIdentifier: Int) {
        synchronized(lock) {
            widgetConfigurationIdentifiersByAppWidgetIdentifier.remove(appWidgetIdentifier)
        }
    }

    private fun assignFirstAvailableWidgetConfiguration(
        appWidgetIdentifier: Int
    ): TappWidgetConfiguration? {
        val firstAvailableWidgetConfigurationIdentifier =
            remoteWidgetConfigurationIdentifiers.firstOrNull()
                ?: return null
        val firstAvailableWidgetConfiguration =
            availableWidgetConfigurations[firstAvailableWidgetConfigurationIdentifier]
                ?: return null

        widgetConfigurationIdentifiersByAppWidgetIdentifier[appWidgetIdentifier] =
            firstAvailableWidgetConfigurationIdentifier
        logD(
            "assignFirstAvailableWidgetConfiguration: appWidgetIdentifier=$appWidgetIdentifier, " +
                "widgetConfigurationIdentifier=$firstAvailableWidgetConfigurationIdentifier"
        )

        return firstAvailableWidgetConfiguration
    }
}
