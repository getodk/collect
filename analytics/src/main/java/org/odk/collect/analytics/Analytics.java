package org.odk.collect.analytics;

public interface Analytics {

    @Deprecated
    void logEvent(String category, String action);

    @Deprecated
    void logEvent(String category, String action, String label);

    void logFormEvent(String event, String formIdHash);

    void logFatal(Throwable throwable);

    void logNonFatal(String message);

    void logServerEvent(String event, String serverHash);

    void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled);

    void setUserProperty(String name, String value);
}
