package org.odk.collect.crashhandler

import android.content.Context

class MockCrashView(context: Context) : CrashView(context) {

    var wasDismissed: Boolean = false
        private set

    override fun dismiss() {
        wasDismissed = true
    }
}
