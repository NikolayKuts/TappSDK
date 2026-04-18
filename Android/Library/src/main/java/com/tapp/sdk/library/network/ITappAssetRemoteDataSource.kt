package com.tapp.sdk.library.network

internal interface ITappAssetRemoteDataSource {

    suspend fun fetchAssetBytes(assetUrl: String): ByteArray
}
