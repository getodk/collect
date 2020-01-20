package org.odk.collect.android.utilities;

import org.odk.collect.android.BuildConfig;

public final class AndroidUserAgent {

    private AndroidUserAgent() {

    }

    public static String getUserAgent() {
        return String.format("%s/%s %s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                System.getProperty("http.agent"));
    }

}
