package org.odk.collect.android.analytics;

import static org.odk.collect.android.analytics.AnalyticsEvents.INVALID_FORM_HASH;
import static org.odk.collect.android.analytics.AnalyticsEvents.SET_SERVER;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.util.Locale;

public final class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static void logServerConfiguration(Analytics analytics, String url) {
        String upperCaseURL = url.toUpperCase(Locale.ENGLISH);
        String scheme = upperCaseURL.split(":")[0];

        String urlHash = Md5.getMd5Hash(new ByteArrayInputStream(url.getBytes()));
        analytics.logEvent(SET_SERVER, scheme + " " + getHostFromUrl(url), urlHash);
    }

    public static void logInvalidFormHash(String url) {
        Analytics.log(INVALID_FORM_HASH, "host", getHostFromUrl(url));
    }

    private static String getHostFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        String upperCaseURL = url.toUpperCase(Locale.ENGLISH);

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
        return host;
    }

    public static String getFormHash(FormController formController) {
        if (formController != null) {
            String formID = formController.getFormDef().getMainInstance().getRoot().getAttributeValue("", "id");
            return getFormHash(formID, formController.getFormTitle());
        } else {
            return "";
        }
    }

    private static String getFormHash(String formId, String formTitle) {
        return Md5.getMd5Hash(new ByteArrayInputStream((formTitle + " " + formId).getBytes()));
    }
}
