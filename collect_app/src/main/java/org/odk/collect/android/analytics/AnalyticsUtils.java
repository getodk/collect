package org.odk.collect.android.analytics;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import static java.lang.String.format;
import static org.odk.collect.android.analytics.AnalyticsEvents.SET_SERVER;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_SERVER_URL;
import static org.odk.collect.forms.FormSourceException.AuthRequired;
import static org.odk.collect.forms.FormSourceException.FetchError;
import static org.odk.collect.forms.FormSourceException.ParseError;
import static org.odk.collect.forms.FormSourceException.SecurityError;
import static org.odk.collect.forms.FormSourceException.ServerError;
import static org.odk.collect.forms.FormSourceException.Unreachable;

public class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static String getServerHash(Settings generalSettings) {
        String currentServerUrl = generalSettings.getString(KEY_SERVER_URL);
        return Md5.getMd5Hash(new ByteArrayInputStream(currentServerUrl.getBytes()));
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

    public static void logServerConfiguration(Analytics analytics, String url) {
        String upperCaseURL = url.toUpperCase(Locale.ENGLISH);
        String scheme = upperCaseURL.split(":")[0];

        String host = "Other";
        if (upperCaseURL.contains("APPSPOT")) {
            host = "Appspot";
        } else if (upperCaseURL.contains("KOBOTOOLBOX.ORG") ||
                upperCaseURL.contains("HUMANITARIANRESPONSE.INFO")) {
            host = "Kobo";
        } else if (upperCaseURL.contains("ONA.IO")) {
            host = "Ona";
        } else if (upperCaseURL.contains("GETODK.CLOUD")) {
            host = "ODK Cloud";
        }

        String urlHash = Md5.getMd5Hash(new ByteArrayInputStream(url.getBytes()));
        analytics.logEvent(SET_SERVER, scheme + " " + host, urlHash);
    }
}
