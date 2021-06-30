package org.odk.collect.analytics

class NoopAnalytics : Analytics {
    override fun logEvent(category: String, action: String) {}
    override fun logEvent(category: String, action: String, label: String) {}

    override fun logEvent(event: String) {}
    override fun logFormEvent(event: String, formIdHash: String) {}
    override fun logFatal(throwable: Throwable) {}
    override fun logNonFatal(message: String) {}
    override fun logServerEvent(event: String, serverHash: String) {}
    override fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {}
    override fun setUserProperty(name: String, value: String) {}
}
