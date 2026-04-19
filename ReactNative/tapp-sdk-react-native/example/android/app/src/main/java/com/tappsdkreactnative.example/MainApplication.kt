package tappsdkreactnative.example

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.tappsdkreactnative.TappSdkReactNativeModule

class MainApplication : Application(), ReactApplication {

  private companion object {

    private const val TAPP_CONFIGURATION_URL =
      "https://drive.google.com/uc?export=download&id=19VNoOAHU0Xegk9Dl99qDVFWRcqwGdx9q"
  }

  override val reactHost: ReactHost by lazy {
    getDefaultReactHost(
      context = applicationContext,
      packageList =
        PackageList(this).packages.apply {
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // add(MyReactNativePackage())
        },
    )
  }

  override fun onCreate() {
    super.onCreate()

    TappSdkReactNativeModule.initialize(
      context = this,
      configurationUrl = TAPP_CONFIGURATION_URL
    )

    loadReactNative(this)
  }
}
