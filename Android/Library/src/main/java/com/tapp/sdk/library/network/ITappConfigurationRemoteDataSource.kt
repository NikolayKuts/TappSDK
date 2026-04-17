package com.tapp.sdk.library.network

internal interface ITappConfigurationRemoteDataSource {

    suspend fun fetchConfigurationJson(configurationUrl: String): String
}
