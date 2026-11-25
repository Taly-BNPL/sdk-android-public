package io.taly.example

import android.app.Application
import io.taly.sdk.TalySdk
import io.taly.sdk.utils.Environment
import io.taly.sdk.utils.logs.LogLevel

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Taly SDK (v1.3): client id and secret come from the Taly merchant portal.
        TalySdk.initialize(
            context = applicationContext,
            clientId = "YOUR_CLIENT_ID",
            clientSecret = "YOUR_CLIENT_SECRET",
            userName = "YOUR_USER_NAME",
            password = "YOUR_PASSWORD",
            environment = Environment.Development
        )
        // Verbose SDK logs in debug builds only; release builds log nothing.
        TalySdk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.SUPRESS)
        // set progress bar color
        TalySdk.setPrimaryColor(R.color.progressColor)
        // set Taly sdk language
        TalySdk.setLanguageCode("en")
    }
}
