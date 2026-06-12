package org.odk.collect.shared.debug

/**
 * Records events. Implementations decide how to surface them - for example as a line in a
 * debug log file and/or an analytics event.
 */
interface DebugLogger<T> {
    fun log(event: T)
}
