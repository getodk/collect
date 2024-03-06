package org.odk.collect.lists

import android.app.Application
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class RobolectricApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true
    }
}
