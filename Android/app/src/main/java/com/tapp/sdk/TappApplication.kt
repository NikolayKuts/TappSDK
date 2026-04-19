package com.tapp.sdk

import android.app.Application
import android.util.Log
import com.tapp.sdk.library.TappSdk

class TappApplication : Application() {

    private companion object {

        private const val TAPP_CONFIGURATION_URL = "https://drive.google.com/uc?export=download&id=19VNoOAHU0Xegk9Dl99qDVFWRcqwGdx9q"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("TappApplication", "onCreate")

        TappSdk.initialize(
            context = this,
            configurationUrl = TAPP_CONFIGURATION_URL
        )
    }
}
