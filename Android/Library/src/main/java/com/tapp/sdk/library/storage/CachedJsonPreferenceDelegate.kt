package com.tapp.sdk.library.storage

import android.content.SharedPreferences
import com.tapp.sdk.library.internal.logD
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T> SharedPreferences.cachedJsonNullable(
    key: String,
    serializer: KSerializer<T>,
    json: Json
): ReadWriteProperty<Any?, T?> {
    return CachedJsonPreferenceDelegate(
        preferences = this,
        key = key,
        serializer = serializer,
        json = json
    )
}

private class CachedJsonPreferenceDelegate<T>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val serializer: KSerializer<T>,
    private val json: Json
) : ReadWriteProperty<Any?, T?> {

    private val lock = Any()
    private var isCached = false
    private var cachedValue: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return synchronized(lock) {
            if (isCached) {
                return@synchronized cachedValue
            }

            val storedJson = preferences.getString(key, null)
            if (storedJson.isNullOrBlank()) {
                isCached = true
                cachedValue = null
                return@synchronized null
            }

            try {
                json.decodeFromString(serializer, storedJson).also { value ->
                    isCached = true
                    cachedValue = value
                }
            } catch (exception: Exception) {
                preferences.edit()
                    .remove(key)
                    .apply()

                isCached = true
                cachedValue = null

                logD(
                    message = "failed to decode cached preference: key=$key",
                    throwable = exception
                )

                null
            }
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        synchronized(lock) {
            if (value == null) {
                preferences.edit()
                    .remove(key)
                    .apply()
            } else {
                preferences.edit()
                    .putString(key, json.encodeToString(serializer, value))
                    .apply()
            }

            isCached = true
            cachedValue = value
        }
    }
}
