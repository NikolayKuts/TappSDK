package com.tapp.sdk.library.storage

import android.content.Context
import java.io.File

internal class FileTappAssetStorage(
    context: Context
) : ITappAssetStorage {

    private val assetsDirectory = File(context.filesDir, ASSETS_DIRECTORY_PATH)

    override suspend fun saveAsset(
        experienceId: String,
        fileName: String,
        bytes: ByteArray
    ): File {
        val assetDirectory = File(assetsDirectory, experienceId)
        assetDirectory.mkdirs()

        val assetFile = File(assetDirectory, fileName)
        assetFile.writeBytes(bytes)

        return assetFile
    }

    override fun getAssetFile(
        experienceId: String,
        fileName: String
    ): File? {
        val assetFile = File(File(assetsDirectory, experienceId), fileName)

        return assetFile.takeIf {
            it.isFile
        }
    }

    private companion object {

        const val ASSETS_DIRECTORY_PATH = "tapp/assets"
    }
}
