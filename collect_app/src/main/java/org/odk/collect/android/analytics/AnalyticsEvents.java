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

    /**
     * Track changes to the Google Sheets fallback submission URL setting. The action should be
     * a hash of the URL.
     */
    public static final String SET_FALLBACK_SHEETS_URL = "SetFallbackSheetsUrl";

    /**
     * Track displays of the splash screen that are not on first launch. The action should be a hash
     * of the splash path.
     */
    public static final String SHOW_SPLASH_SCREEN = "ShowSplashScreen";

    /**
     * Track video requests with high resolution setting turned off. The action should be a hash of
     * the form definition.
     */
    public static final String REQUEST_VIDEO_NOT_HIGH_RES = "RequestVideoNotHighRes";

    /**
     * Track video requests with high resolution setting turned on. This is tracked to contextualize
     * the counts with the high resolution setting turned off since we expect that video is not very
     * common overall. The action should be a hash of the form definition.
     */
    public static final String REQUEST_HIGH_RES_VIDEO = "RequestHighResVideo";

    /**
     * Track submission encryption. The action should be a hash of the form definition.
     */
    public static final String ENCRYPT_SUBMISSION = "EncryptSubmission";
}
