package org.odk.collect.android.analytics;

import static org.odk.collect.android.analytics.AnalyticsEvents.SET_SERVER;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_SERVER_URL;
import static org.odk.collect.forms.FormSourceException.AuthRequired;
import static org.odk.collect.forms.FormSourceException.FetchError;
import static org.odk.collect.forms.FormSourceException.ParseError;
import static org.odk.collect.forms.FormSourceException.SecurityError;
import static org.odk.collect.forms.FormSourceException.ServerError;
import static org.odk.collect.forms.FormSourceException.Unreachable;
import static java.lang.String.format;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.util.Locale;

public final class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static void setForm(FormController formController) {
        Analytics.setParam("form", getFormHash(formController));
    }

    public static void logFormEvent(String event) {
        Analytics.log(event, "form");
    }

    public static void logFormEvent(String event, String formId, String formTitle) {
        Analytics.log(event, "form", getFormHash(formId, formTitle));
    }

    public static void logServerEvent(String event, Settings generalSettings) {
        Analytics.log(event, "server", getServerHash(generalSettings));
    }

    public static void logMatchExactlyCompleted(Analytics analytics, FormSourceException exception) {
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, getFormSourceExceptionAction(exception));
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

    public static String getFormHash(String formId, String formTitle) {
        return Md5.getMd5Hash(new ByteArrayInputStream((formTitle + " " + formId).getBytes()));
    }

    public static String getFormHash(FormController formController) {
        if (formController != null) {
            String formID = formController.getFormDef().getMainInstance().getRoot().getAttributeValue("", "id");
            return getFormHash(formID, formController.getFormTitle());
        } else {
            return "";
        }
    }

    private static String getServerHash(Settings generalSettings) {
        String currentServerUrl = generalSettings.getString(KEY_SERVER_URL);
        return Md5.getMd5Hash(new ByteArrayInputStream(currentServerUrl.getBytes()));
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
