package org.odk.collect.android.analytics;

import org.odk.collect.android.forms.FormSourceException;

import static java.lang.String.format;

public class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static void logMatchExactlyCompleted(Analytics analytics, FormSourceException formSourceException) {
        if (formSourceException == null) {
            analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "Success");
        } else if (formSourceException.getType().equals(FormSourceException.Type.SERVER_ERROR)) {
            analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, format("SERVER_ERROR_%s", formSourceException.getStatusCode()));
        } else {
            analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, formSourceException.getType().toString());
        }
    }
}
