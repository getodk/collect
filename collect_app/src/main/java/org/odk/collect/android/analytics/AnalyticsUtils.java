package org.odk.collect.android.analytics;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.forms.FormSourceException;

import static java.lang.String.format;
import static org.odk.collect.android.forms.FormSourceException.AuthRequired;
import static org.odk.collect.android.forms.FormSourceException.FetchError;
import static org.odk.collect.android.forms.FormSourceException.ParseError;
import static org.odk.collect.android.forms.FormSourceException.SecurityError;
import static org.odk.collect.android.forms.FormSourceException.ServerError;
import static org.odk.collect.android.forms.FormSourceException.Unreachable;

public class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static void logMatchExactlyCompleted(Analytics analytics, FormSourceException exception) {
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, getFormSourceExceptionAction(exception));
    }

    private static String getFormSourceExceptionAction(FormSourceException exception) {
        if (exception == null) {
            return "Success";
        } else if (exception instanceof Unreachable) {
            return "UNREACHABLE";
        } else if (exception instanceof AuthRequired) {
            return "AUTH_REQUIRED";
        } else if (exception instanceof ServerError) {
            return format("SERVER_ERROR_%s", ((ServerError) exception).getStatusCode());
        } else if (exception instanceof SecurityError) {
            return "SECURITY_ERROR";
        } else if (exception instanceof ParseError) {
            return "PARSE_ERROR";
        } else if (exception instanceof FetchError) {
            return "FETCH_ERROR";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
