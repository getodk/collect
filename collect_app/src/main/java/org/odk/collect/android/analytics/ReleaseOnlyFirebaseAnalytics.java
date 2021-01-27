package org.odk.collect.android.analytics;

import android.os.Bundle;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.odk.collect.android.BuildConfig;

public class ReleaseOnlyFirebaseAnalytics implements Analytics {

    private final com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics;
    private final FirebaseCrashlytics crashlytics;

    public ReleaseOnlyFirebaseAnalytics(com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics, FirebaseCrashlytics crashlytics) {
        this.firebaseAnalytics = firebaseAnalytics;
        this.crashlytics = crashlytics;
    }

    @Deprecated
    @Override
    public void logEvent(String category, String action) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        firebaseAnalytics.logEvent(category, bundle);
    }

    @Deprecated
    @Override
    public void logEvent(String category, String action, String label) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        bundle.putString("label", label);
        firebaseAnalytics.logEvent(category, bundle);
    }

    @Override
    public void logFormEvent(String event, String formId) {
        Bundle bundle = new Bundle();
        bundle.putString("form", formId);
        firebaseAnalytics.logEvent(event, bundle);
    }

    @Override
    public void logFatal(Throwable throwable) {
        crashlytics.recordException(throwable);
    }

    @Override
    public void logNonFatal(String message) {
        crashlytics.log(message);
    }

    @Override
    public void logServerEvent(String event, String serverHash) {
        Bundle bundle = new Bundle();
        bundle.putString("server", serverHash);
        firebaseAnalytics.logEvent(event, bundle);
    }

    public void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled) {
        if (BuildConfig.BUILD_TYPE.equals("odkCollectRelease")) {
            firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled);
            crashlytics.setCrashlyticsCollectionEnabled(isAnalyticsEnabled);
        } else {
            firebaseAnalytics.setAnalyticsCollectionEnabled(false);
            crashlytics.setCrashlyticsCollectionEnabled(false);
        }
    }

    @Override
    public void setUserProperty(String name, String value) {
        firebaseAnalytics.setUserProperty(name, value);
    }
}
