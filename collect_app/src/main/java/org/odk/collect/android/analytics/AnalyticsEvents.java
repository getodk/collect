package org.odk.collect.android.analytics;

public class AnalyticsEvents {

    private AnalyticsEvents() {

    }

    /**
     * Used to measure how popular different methods of adding repeats are. The "action" should
     * describe the way the repeat was added. The label should be the form hash identifier.
     */
    public static final String ADD_REPEAT = "AddRepeat";

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

    /**
     * Track changes to the filter applied to the list of forms to send. The action should be a
     * filter.
     */
    public static final String FILTER_FORMS_TO_SEND = "FilterSendForms";

    /**
     * Track displays of the likert question type. The action should be a hash of the form definition.
     */
    public static final String LIKERT = "Likert";

    /**
     * Track null form controllers. The action should be a description of when this occurred.
     */
    public static final String NULL_FORM_CONTROLLER_EVENT = "NullFormControllerEvent";

    /**
     * Track changes to preferences related to automatic form updates. The action should be the
     * preference name and the label should be the new preference value.
     */
    public static final String AUTO_FORM_UPDATE_PREF_CHANGE = "PreferenceChange";

    /**
     * Track submissions. The action should describe how it's being sent and the label should be a
     * hash of the form definition.
     */
    public static final String SUBMISSION = "Submission";

    /**
     * Track form definitions with the saveIncomplete attribute. The action should be saveIncomplete
     * and the label should be a hash of the form definition.
     */
    public static final String SAVE_INCOMPLETE = "WidgetAttribute";

    /**
     * Track displays of audio question types. The action should be the type of audio question and
     * the label should be a hash of the form definition.
     */
    public static final String AUDIO_QUESTION = "Prompt";

    /**
     * Track initiations of a configuration QR code scan. The action should describe where the scan
     * was launched from.
     */
    public static final String SCAN_QR_CODE = "ScanQRCode";

    /**
     * Track launches of form definitions that have a background location action. The action should
     * be a hash of the form definition.
     */
    public static final String LAUNCH_FORM_WITH_BG_LOCATION = "LaunchFormWithBGLocation";

    /**
     * Track scoped storage migration attempts. The action should be the result of the attempt.
     */
    public static final String SCOPED_STORAGE_MIGRATION = "ScopedStorageMigration";
}
