package org.odk.collect.shared

interface DebugLogger {
    fun log(tag: String, message: String)

    fun logWithAnalytics(tag: String, message: String, analyticsEvent: String, analyticsKey: String)
}
