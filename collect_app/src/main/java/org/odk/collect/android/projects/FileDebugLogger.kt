package org.odk.collect.android.projects

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.BuildConfig
import org.odk.collect.shared.DebugLogger
import java.io.File
import java.time.LocalDateTime

class FileDebugLogger(private val file: File) : DebugLogger {

    override fun log(tag: String, message: String) {
        logToFile(tag, message)
    }

    override fun logWithAnalytics(tag: String, message: String, analyticsEvent: String, analyticsKey: String) {
        logToFile(tag, message)
        Analytics.log(analyticsEvent, analyticsKey)
    }

    private fun logToFile(tag: String, message: String) {
        if (enabled) {
            val line = "${LocalDateTime.now()} $tag \"$message\"\n"
            file.appendText(line)
        }
    }

    companion object {
        private val enabled = BuildConfig.DEBUG
    }
}
