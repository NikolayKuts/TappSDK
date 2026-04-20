package com.tappsdkreactnative

import com.facebook.react.bridge.ReactApplicationContext
import com.tapp.sdk.library.TappSdk

class TappSdkReactNativeModule(
  reactContext: ReactApplicationContext
) : NativeTappSdkReactNativeSpec(reactContext) {

  companion object {

    const val NAME = NativeTappSdkReactNativeSpec.NAME
  }

  override fun initialize(configurationUrl: String) {
    TappSdk.initialize(
      context = reactApplicationContext,
      configurationUrl = configurationUrl
    )
  }
}
