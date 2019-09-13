package org.odk.collect.android.analytics;

import android.os.Bundle;

public class FirebaseAnalytics implements Analytics {

    private final com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics;

    public FirebaseAnalytics(com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    @Override
    public void logEvent(String category, String action) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        firebaseAnalytics.logEvent(category, bundle);
    }

    @Override
    public void logEvent(String category, String action, String label) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        bundle.putString("label", label);
        firebaseAnalytics.logEvent(category, bundle);
    }
}
