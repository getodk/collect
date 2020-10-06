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
     * Track displays of widget/question types. The action should be the type of widget and
     * the label should be a hash of the form definition.
     */
    public static final String PROMPT = "Prompt";

    /**
     * Track scoped storage migration attempts. The action should be the result of the attempt.
     */
    public static final String SCOPED_STORAGE_MIGRATION = "ScopedStorageMigration";

    /**
     * Track downloads initiated when there are no downloaded forms on the device. The action should
     * be in the format: {number of downloaded forms}/{total forms}-{form server hash}
     *
     * Questions to answer to help shape new on-boarding and multi-tenancy experience:
     *      - Does it look like some projects instruct data collectors to download all forms on
     *      first launch and others to download a subset?
     *      - If it looks like there's a clear process split, which of the two processes is most
     *      common? Is project scale or number of forms hosted on server relevant to the process?
     *      - Are subsequent manual downloads common? Are all forms downloaded or a subset?
     */
    public static final String FIRST_FORM_DOWNLOAD = "FirstFormDownload";

    /**
     * Download a subset of available forms. The action should be in the format:
     * {number of downloaded forms}/{total forms}-{form server hash}
     */
    public static final String SUBSEQUENT_FORM_DOWNLOAD = "SubsequentFormDownload";

    /**
     * Used to measure how popular the refresh button on Fill Blank Forms is. The button
     * only displays when Match Exactly is enabled. Right now the action passed is "Manual". This
     * means that we could extend the event to track auto or other syncs in the future.
     */
    public static final String MATCH_EXACTLY_SYNC = "MatchExactlySync";

    /**
     * Used to measure how the relative frequencies of different outcomes for a Match Exactly sync.
     * The action should indicate the outcome as any of:
     * - "Success"
     * - "UNKNOWN_HOST"
     * - "AUTH_REQUIRED"
     * - "FETCH_ERROR"
     */
    public static final String MATCH_EXACTLY_SYNC_COMPLETED = "MatchExactlySyncCompleted";

    /**
     * Track the outcome of a QR code scan. Used to see whether changes in UX or documentation are
     * needed to get users to a successful state. The action should be the outcome. The label should
     * be a hash of the settings represented by the code.
     */
    public static final String SETTINGS_IMPORT_QR = "SettingsImportQr";

    /**
     * Track the outcome of a QR code read from image. The action should be the outcome. The label should
     * be a hash of the settings represented by the code.
     */
    public static final String SETTINGS_IMPORT_QR_IMAGE = "SettingsImportQrImage";

    /**
     * Track the outcome of a .collect.settings file being imported. The action should be the outcome.
     * The label should be a hash of the settings represented by the file.
     */
    public static final String SETTINGS_IMPORT_SERIALIZED = "SettingsImportSerialized";

    /**
     * Track the outcome of a .collect.settings.json file being imported. The action should be the outcome.
     * The label should be a hash of the settings represented by the file.
     */
    public static final String SETTINGS_IMPORT_JSON = "SettingsImportJson";

    /**
     * Track submissions to a URL with a custom submission endpoint configured in settings. The action
     * should be a hash of the endpoint setting.
     */
    public static final String CUSTOM_ENDPOINT_SUB = "CustomEndpointSub";
}
