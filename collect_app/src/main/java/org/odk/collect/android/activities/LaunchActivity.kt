package org.odk.collect.android.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.odk.collect.android.application.Collect
import org.odk.collect.strings.localization.LocalizedActivity

/**
 * This Activity is needed to replicate the new(er) Splash Screen behaviour on older Android
 * versions.
 *
 * **See Also:** [https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen](https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen)
 */
class LaunchActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val crashHandler = (application as Collect).crashHandler
        val crashView = crashHandler.getCrashView(this, ::launchApp)
        if (crashView != null) {
            setContentView(crashView)
        } else {
            launchApp()
        }
    }

    private fun launchApp() {
        startActivity(Intent(this, MainMenuActivity::class.java))
    }
}
