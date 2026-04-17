package com.tapp.sdk.library.data.mapper

import com.tapp.sdk.library.data.contract.TappConfigurationContract
import com.tapp.sdk.library.data.contract.TappContentViewTypeContract
import com.tapp.sdk.library.data.contract.TappExperienceContract
import com.tapp.sdk.library.data.contract.TappMetaContract
import com.tapp.sdk.library.data.contract.TappNetworkAssetsContract
import com.tapp.sdk.library.data.contract.TappNetworkAttributesContract
import com.tapp.sdk.library.data.contract.TappNetworkContract
import com.tapp.sdk.library.data.contract.TappSurfaceTypeContract
import com.tapp.sdk.library.data.contract.TappWheelAssetsContract
import com.tapp.sdk.library.data.contract.TappWheelContract
import com.tapp.sdk.library.data.contract.TappWheelRotationContract
import com.tapp.sdk.library.domain.TappConfiguration
import com.tapp.sdk.library.domain.TappContentViewType
import com.tapp.sdk.library.domain.TappExperience
import com.tapp.sdk.library.domain.TappMeta
import com.tapp.sdk.library.domain.TappNetwork
import com.tapp.sdk.library.domain.TappNetworkAssets
import com.tapp.sdk.library.domain.TappNetworkAttributes
import com.tapp.sdk.library.domain.TappSurfaceType
import com.tapp.sdk.library.domain.TappWheel
import com.tapp.sdk.library.domain.TappWheelAssets
import com.tapp.sdk.library.domain.TappWheelRotation

internal fun TappConfigurationContract.toDomain(): TappConfiguration {
    return TappConfiguration(
        data = data.mapNotNull { it.toDomain() },
        meta = meta?.toDomain()
    )
}

private fun TappExperienceContract.toDomain(): TappExperience? {
    val experienceIdentifier = id ?: return null
    val experienceName = name ?: return null
    val experienceSurfaceType = type?.toDomain() ?: return null
    val wheelConfiguration = wheel?.toDomain()
    val contentViewType = when {
        wheelConfiguration != null -> TappContentViewType.Wheel
        else -> return null
    }

    return TappExperience(
        id = experienceIdentifier,
        name = experienceName,
        surfaceType = experienceSurfaceType,
        contentViewType = contentViewType,
        network = network?.toDomain(),
        wheel = wheelConfiguration
    )
}

private fun TappSurfaceTypeContract.toDomain(): TappSurfaceType {
    return when (this) {
        TappSurfaceTypeContract.Widget -> TappSurfaceType.Widget
        TappSurfaceTypeContract.Notification -> TappSurfaceType.Notification
    }
}

private fun TappContentViewTypeContract.toDomain(): TappContentViewType {
    return when (this) {
        TappContentViewTypeContract.Wheel -> TappContentViewType.Wheel
        TappContentViewTypeContract.Banner -> TappContentViewType.Banner
    }
}

private fun TappNetworkContract.toDomain(): TappNetwork? {
    val networkAttributes = attributes?.toDomain()
    val networkAssets = assets?.toDomain()

    if (networkAttributes == null && networkAssets == null) {
        return null
    }

    return TappNetwork(
        attributes = networkAttributes,
        assets = networkAssets
    )
}

private fun TappNetworkAttributesContract.toDomain(): TappNetworkAttributes? {
    return TappNetworkAttributes(
        refreshInterval = refreshInterval ?: return null,
        networkTimeout = networkTimeout ?: return null,
        retryAttempts = retryAttempts ?: return null,
        cacheExpiration = cacheExpiration ?: return null,
        debugMode = debugMode ?: return null
    )
}

private fun TappNetworkAssetsContract.toDomain(): TappNetworkAssets? {
    return TappNetworkAssets(
        host = host ?: return null
    )
}

private fun TappWheelContract.toDomain(): TappWheel? {
    return TappWheel(
        rotation = rotation?.toDomain() ?: return null,
        assets = assets?.toDomain() ?: return null
    )
}

private fun TappWheelRotationContract.toDomain(): TappWheelRotation? {
    return TappWheelRotation(
        duration = duration ?: return null,
        minimumSpins = minimumSpins ?: return null,
        maximumSpins = maximumSpins ?: return null,
        spinEasing = spinEasing ?: return null
    )
}

private fun TappWheelAssetsContract.toDomain(): TappWheelAssets? {
    return TappWheelAssets(
        background = bg ?: return null,
        wheelFrame = wheelFrame ?: return null,
        wheelSpin = wheelSpin ?: return null,
        wheel = wheel ?: return null
    )
}

private fun TappMetaContract.toDomain(): TappMeta? {
    return TappMeta(
        version = version ?: return null,
        copyright = copyright ?: return null
    )
}
