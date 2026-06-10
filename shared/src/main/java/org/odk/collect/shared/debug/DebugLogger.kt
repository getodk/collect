package org.odk.collect.shared.debug

/**
 * Records [DebugEvent]s. Implementations decide how to surface them - for example as a line in a
 * debug log file and/or an analytics event.
 */
interface DebugLogger {
    fun log(event: DebugEvent)
}
