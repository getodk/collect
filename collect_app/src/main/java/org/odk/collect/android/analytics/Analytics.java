package org.odk.collect.android.analytics;

public interface Analytics {

    void logEvent(String event, String action);

    void logEvent(String event, String action, String label);
}