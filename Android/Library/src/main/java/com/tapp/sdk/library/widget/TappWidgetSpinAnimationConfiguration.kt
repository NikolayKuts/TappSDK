package com.tapp.sdk.library.widget

import com.tapp.sdk.library.domain.TappSpinEasing

internal data class TappWidgetSpinAnimationConfiguration(
    val durationMilliseconds: Long,
    val minimumSpins: Float,
    val maximumSpins: Float,
    val spinEasing: TappSpinEasing
)
