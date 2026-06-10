package org.odk.collect.entities.debug

import org.odk.collect.analytics.Analytics
import org.odk.collect.entities.BuildConfig
import org.odk.collect.shared.debug.DebugEvent
import org.odk.collect.shared.debug.DebugLogger
import java.io.File
import java.time.LocalDateTime

/**
 * A [DebugLogger] that writes each event both as a line in a debug log file (in debug builds) and
 * as an analytics event.
 */
class EntitiesDebugLogger(private val file: File) : DebugLogger {

    override fun log(event: DebugEvent) {
        if (BuildConfig.DEBUG) {
            val line = "${LocalDateTime.now()} Entities \"${event.message}\"\n"
            file.appendText(line)
        }

        Analytics.log(event.analyticsEvent, "form")
    }
}
