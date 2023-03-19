package org.odk.collect.android.application.initialization

import android.util.Log
import org.odk.collect.analytics.Analytics
import timber.log.Timber

internal class CrashReportingTree(private val analytics: Analytics) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.WARN -> analytics.logMessage("W/$tag: $message")

            Log.ERROR -> analytics.logNonFatal(t ?: Error("E/$tag: $message"))
        }
    }
}
