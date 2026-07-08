package org.odk.collect.analytics

class NoopAnalytics : Analytics {
    override fun logEvent(event: String) {}
    override fun logEventWithParams(event: String, params: Map<String, String>) {}
    override fun logNonFatal(throwable: Throwable) {}
    override fun logMessage(message: String) {}
    override fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {}
    override fun setUserProperty(name: String, value: String) {}
}
