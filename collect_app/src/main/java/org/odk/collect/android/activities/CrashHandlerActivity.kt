package org.odk.collect.android.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.androidshared.ui.EdgeToEdge.setView
import org.odk.collect.crashhandler.CrashHandler
import org.odk.collect.strings.localization.LocalizedActivity

class CrashHandlerActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashHandler = CrashHandler.getInstance(this)!!
        val crashView = crashHandler.getCrashView(this) {
            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        }

        if (crashView != null) {
            setView(crashView, false)
        } else {
            finish()
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    crashView?.dismiss()
                }
            }
        )
    }
}
