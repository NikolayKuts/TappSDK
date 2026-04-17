package com.tapp.sdk.library.storage

import com.tapp.sdk.library.domain.TappConfiguration

internal object InMemoryTappConfigurationStorage : ITappConfigurationStorage {

    private val lock = Any()
    private var configuration: TappConfiguration? = null
    private var lastFetchTimeMillis: Long? = null

    override suspend fun saveConfiguration(configuration: TappConfiguration) {
        synchronized(lock) {
            this.configuration = configuration
        }
    }

    override suspend fun getConfiguration(): TappConfiguration? {
        return synchronized(lock) {
            configuration
        }
    }

    override suspend fun saveLastFetchTimeMillis(lastFetchTimeMillis: Long) {
        synchronized(lock) {
            this.lastFetchTimeMillis = lastFetchTimeMillis
        }
    }

    override suspend fun getLastFetchTimeMillis(): Long? {
        return synchronized(lock) {
            lastFetchTimeMillis
        }
    }
}
