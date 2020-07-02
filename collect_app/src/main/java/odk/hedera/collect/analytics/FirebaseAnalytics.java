package odk.hedera.collect.analytics;

import android.os.Bundle;

import odk.hedera.collect.preferences.GeneralKeys;
import odk.hedera.collect.preferences.GeneralSharedPreferences;

public class FirebaseAnalytics implements Analytics {

    private final com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics;
    private final GeneralSharedPreferences generalSharedPreferences;

    public FirebaseAnalytics(com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics, GeneralSharedPreferences generalSharedPreferences) {
        this.firebaseAnalytics = firebaseAnalytics;
        this.generalSharedPreferences = generalSharedPreferences;
        setupRemoteAnalytics();
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

    private void setupRemoteAnalytics() {
        boolean isAnalyticsEnabled = generalSharedPreferences.getBoolean(GeneralKeys.KEY_ANALYTICS, true);
        setAnalyticsCollectionEnabled(isAnalyticsEnabled);
    }

    public void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled);
    }
}
