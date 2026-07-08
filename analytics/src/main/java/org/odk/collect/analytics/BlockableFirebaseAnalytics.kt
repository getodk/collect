package org.odk.collect.analytics

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class BlockableFirebaseAnalytics(application: Application, private val crashReports: Boolean) : Analytics {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(application)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    override fun logEventWithParams(
        event: String,
        params: Map<String, String>
    ) {
        val bundle = params.entries.fold(Bundle()) { bundle, entry ->
            bundle.putString(entry.key, entry.value)
            bundle
        }

        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun logNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun logMessage(message: String) {
        crashlytics.log(message)
    }

    override fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled)
        if (!crashReports) {
            crashlytics.isCrashlyticsCollectionEnabled = isAnalyticsEnabled
        }
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
