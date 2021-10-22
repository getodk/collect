package org.odk.collect.testshared

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.odk.collect.androidshared.utils.IntentLauncher

class DummyIntentLauncher : IntentLauncher {
    override fun launch(context: Context, intent: Intent, onError: () -> Unit) {
    }

    override fun launchForResult(
        activity: Activity,
        intent: Intent,
        requestCode: Int,
        onError: () -> Unit
    ) {
    }
}

class ErrorIntentLauncher : IntentLauncher {
    override fun launch(context: Context, intent: Intent, onError: () -> Unit) {
        onError()
    }

    override fun launchForResult(
        activity: Activity,
        intent: Intent,
        requestCode: Int,
        onError: () -> Unit
    ) {
        onError()
    }
}
