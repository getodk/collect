package org.odk.collect.analytics

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class BlockableFirebaseAnalytics(application: Application) : Analytics {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application)
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Deprecated("")
    override fun logEvent(category: String, action: String) {
        val bundle = Bundle()
        bundle.putString("action", action)
        firebaseAnalytics.logEvent(category, bundle)
    }

    @Deprecated("")
    override fun logEvent(category: String, action: String, label: String) {
        val bundle = Bundle()
        bundle.putString("action", action)
        bundle.putString("label", label)
        firebaseAnalytics.logEvent(category, bundle)
    }

    override fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    override fun logFormEvent(event: String, formIdHash: String) {
        val bundle = Bundle()
        bundle.putString("form", formIdHash)
        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun logFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun logNonFatal(message: String) {
        crashlytics.log(message)
    }

    override fun logServerEvent(event: String, serverHash: String) {
        val bundle = Bundle()
        bundle.putString("server", serverHash)
        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled)
        crashlytics.setCrashlyticsCollectionEnabled(isAnalyticsEnabled)
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
