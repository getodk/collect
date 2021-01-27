package org.odk.collect.analytics;

public class NoopAnalytics implements Analytics {

    @Override
    public void logEvent(String category, String action) {

    }

    @Override
    public void logEvent(String category, String action, String label) {

    }

    @Override
    public void logFormEvent(String event, String formIdHash) {

    }

    @Override
    public void logFatal(Throwable throwable) {

    }

    @Override
    public void logNonFatal(String message) {

    }

    @Override
    public void logServerEvent(String event, String serverHash) {

    }

    @Override
    public void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled) {

    }

    @Override
    public void setUserProperty(String name, String value) {

    }
}
