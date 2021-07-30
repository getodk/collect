package org.odk.collect.analytics

interface Analytics {
    @Deprecated("")
    fun logEvent(category: String, action: String)

    @Deprecated("")
    fun logEvent(category: String, action: String, label: String)

    fun logEvent(event: String)
    fun logEventWithParam(event: String, key: String, value: String)
    fun logFatal(throwable: Throwable)
    fun logNonFatal(message: String)
    fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean)
    fun setUserProperty(name: String, value: String)

    companion object {

        private var instance: Analytics = NoopAnalytics()

        fun setInstance(analytics: Analytics) {
            this.instance = analytics
        }

        @JvmStatic
        fun log(event: String) {
            instance.logEvent(event)
        }

        @JvmStatic
        fun log(event: String, key: String, value: String) {
            instance.logEventWithParam(event, key, value)
        }
    }
}
