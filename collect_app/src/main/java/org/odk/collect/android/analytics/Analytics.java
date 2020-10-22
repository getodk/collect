package org.odk.collect.android.analytics;

public interface Analytics {
    
    void logEvent(String category, String action);

    void logEvent(String category, String action, String label);

    void logFormEvent(String event, String formId);

    void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled);

    void setUserProperty(String name, String value);
}
