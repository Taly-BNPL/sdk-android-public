package io.taly.example

import android.app.Application
import com.facebook.stetho.Stetho
import io.taly.sdk.TalySdk
import io.taly.sdk.utils.Environment
import io.taly.sdk.utils.logs.LogLevel

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        // Initialize Taly SDK
        TalySdk.initialize(
            context = applicationContext,
            userName = "YOUR_USER_NAME",
            password = "YOUR_PASSWORD",
            environment = Environment.Development
        )
        // set log level for debugging.
        TalySdk.setLogLevel(LogLevel.VERBOSE)
        // set progress bar color
        TalySdk.setPrimaryColor(R.color.progressColor)
        // set Taly sdk language
        TalySdk.setLanguageCode("en")
    }
}