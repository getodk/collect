package org.odk.collect.android.activities

import android.os.Bundle
import org.odk.collect.crash_handler.CrashHandler
import org.odk.collect.strings.localization.LocalizedActivity

class CrashHandlerActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashView = CrashHandler.getInstance(this)?.getCrashView(this) {
            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        }

        setContentView(crashView)
    }
}
