package org.odk.collect.analytics

class NoopAnalytics : Analytics {
    override fun logEvent(category: String, action: String) {}
    override fun logEvent(category: String, action: String, label: String) {}

    override fun logEvent(event: String) {}
    override fun logEventWithParam(event: String, key: String, value: String) {}
    override fun logFatal(throwable: Throwable) {}
    override fun logNonFatal(message: String) {}
    override fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {}
    override fun setUserProperty(name: String, value: String) {}
}
