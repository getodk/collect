package org.odk.collect.android.analytics;

public class AnalyticsEvents {
    private AnalyticsEvents() {
    }

    /**
     * Track changes to the server URL setting. The action should be the scheme followed by a space
     * followed by one of Appspot, Ona, Kobo or Other. The label should be a hash of the URL.
     */
    public static final String SET_SERVER = "SetServer";

    /**
     * Track changes to the custom formList or submission endpoint settings. The action should be
     * the standard endpoint name followed by a space followed by the hash of the custom endpoint
     * name.
     */
    public static final String SET_CUSTOM_ENDPOINT = "SetCustomEndpoint";


}
