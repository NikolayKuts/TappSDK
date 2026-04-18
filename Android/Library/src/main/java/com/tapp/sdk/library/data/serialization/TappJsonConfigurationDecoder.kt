package com.tapp.sdk.library.data.serialization

import com.tapp.sdk.library.data.contract.TappConfigurationContract
import kotlinx.serialization.json.Json

internal object TappJsonConfigurationDecoder {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun decode(configurationJson: String): TappConfigurationContract {
        return json.decodeFromString<TappConfigurationContract>(configurationJson)
    }
}
