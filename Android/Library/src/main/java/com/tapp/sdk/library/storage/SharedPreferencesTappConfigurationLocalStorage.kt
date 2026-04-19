package com.tapp.sdk.library.storage

import android.content.Context
import android.content.SharedPreferences
import com.tapp.sdk.library.domain.TappConfiguration
import kotlinx.serialization.json.Json

private const val TAPP_CONFIGURATION_PREFERENCES_NAME = "tapp_sdk_configuration"
private const val CACHED_CONFIGURATION_KEY = "cached_configuration"
private const val CONFIGURATION_FETCHED_AT_MILLIS_KEY = "configuration_fetched_at_millis"

internal class SharedPreferencesTappConfigurationLocalStorage(
    context: Context,
    json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }
) : ITappConfigurationLocalStorage {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        TAPP_CONFIGURATION_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override var cachedConfiguration: TappConfiguration? by preferences.cachedJsonNullable(
        key = CACHED_CONFIGURATION_KEY,
        serializer = TappConfiguration.serializer(),
        json = json
    )

    override var configurationFetchedAtMillis: Long?
        get() {
            if (!preferences.contains(CONFIGURATION_FETCHED_AT_MILLIS_KEY)) {
                return null
            }

            return preferences.getLong(CONFIGURATION_FETCHED_AT_MILLIS_KEY, 0L)
        }
        set(value) {
            val editor = preferences.edit()

            if (value == null) {
                editor.remove(CONFIGURATION_FETCHED_AT_MILLIS_KEY)
            } else {
                editor.putLong(CONFIGURATION_FETCHED_AT_MILLIS_KEY, value)
            }

            editor.apply()
        }
}
