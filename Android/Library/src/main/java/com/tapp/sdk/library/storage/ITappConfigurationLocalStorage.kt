package com.tapp.sdk.library.storage

import com.tapp.sdk.library.domain.TappConfiguration

internal interface ITappConfigurationLocalStorage {

    var cachedConfiguration: TappConfiguration?

    var configurationFetchedAtMillis: Long?
}
