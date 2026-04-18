package com.tappsdkreactnative

import com.facebook.react.bridge.ReactApplicationContext

class TappSdkReactNativeModule(reactContext: ReactApplicationContext) :
  NativeTappSdkReactNativeSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeTappSdkReactNativeSpec.NAME
  }
}
