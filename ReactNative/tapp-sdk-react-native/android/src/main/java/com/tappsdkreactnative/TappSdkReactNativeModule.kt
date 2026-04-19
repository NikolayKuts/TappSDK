package com.tappsdkreactnative

import android.content.Context
import com.facebook.react.bridge.ReactApplicationContext
import com.tapp.sdk.library.TappSdk

class TappSdkReactNativeModule(
  reactContext: ReactApplicationContext
) : NativeTappSdkReactNativeSpec(reactContext) {

  companion object {

    const val NAME = NativeTappSdkReactNativeSpec.NAME

    fun initialize(
      context: Context,
      configurationUrl: String
    ) {
      TappSdk.initialize(
        context = context.applicationContext,
        configurationUrl = configurationUrl
      )
    }
  }

  override fun initialize(configurationUrl: String) {
    initialize(
      context = reactApplicationContext,
      configurationUrl = configurationUrl
    )
  }
}
