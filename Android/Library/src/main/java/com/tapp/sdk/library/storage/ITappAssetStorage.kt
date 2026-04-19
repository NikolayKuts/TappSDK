package com.tapp.sdk.library.storage

import java.io.File

internal interface ITappAssetStorage {

    suspend fun saveAsset(
        experienceId: String,
        fileName: String,
        bytes: ByteArray
    ): File

    fun getAssetFile(
        experienceId: String,
        fileName: String
    ): File?
}
