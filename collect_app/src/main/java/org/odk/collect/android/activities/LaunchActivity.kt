package org.odk.collect.android.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

        startActivity(Intent(this, MainMenuActivity::class.java))
    }
}
