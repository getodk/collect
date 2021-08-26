package org.odk.collect.android.analytics;

public final class AnalyticsEvents {

    private AnalyticsEvents() {

    }

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
     * Track attempts to download a form with the same formid/version but different contents as one
     * already on the device. We know this happens in the case of Central drafts but it should
     * otherwise be rare.
     */
    public static final String DOWNLOAD_SAME_FORMID_VERSION_DIFFERENT_HASH = "DownloadSameFormidVersionDifferentHash";

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
     * Tracks how often the audio player seek bar is used.
     */
    public static final String AUDIO_PLAYER_SEEK = "AudioPlayerSeek";

    /**
     * Tracks how often pause action is used while recording audio
     */
    public static final String AUDIO_RECORDING_PAUSE = "AudioRecordingPause";

    /**
     * Tracks usage or internal recording vs external recording vs choosing files for
     * audio question
     */
    public static final String AUDIO_RECORDING_INTERNAL = "AudioRecordingInternal";
    public static final String AUDIO_RECORDING_EXTERNAL = "AudioRecordingExternal";
    public static final String AUDIO_RECORDING_CHOOSE = "AudioRecordingChoose";

    /**
     * Tracks how often questions are answered while an audio recording is being made
     */
    public static final String ANSWER_WHILE_RECORDING = "AnswerWhileRecording";

    /**
     * Tracks how many users have opted in to internal recording as their default
     */
    public static final String INTERNAL_RECORDING_OPT_IN = "InternalRecordingOptIn";

    /**
     * Track how many forms record background audio
     */
    public static final String REQUESTS_BACKGROUND_AUDIO = "RequestsBackgroundAudio";

    /**
     * Track how often background audio is disabled for a form
     */
    public static final String BACKGROUND_AUDIO_ENABLED = "BackgroundAudioEnabled";

    /**
     * Track how often background audio is enabled for a form
     */
    public static final String BACKGROUND_AUDIO_DISABLED = "BackgroundAudioDisabled";

    /**
     * Tracks if any forms are being used as part of a workflow where instances are imported
     * from disk
     */
    public static final String IMPORT_INSTANCE = "ImportInstance";

    /**
     * Tracks if any forms are being used as part of a workflow where instances are imported
     * from disk and then encrypted
     */
    public static final String IMPORT_AND_ENCRYPT_INSTANCE = "ImportAndEncryptInstance";

    /**
     * Tracks how often forms are added using disk sync rather than from a server
     */
    public static final String IMPORT_FORM = "ImportForm";

    /**
     * Tracks responses from OpenMapKit to the OSMWidget
     */
    public static final String OPEN_MAP_KIT_RESPONSE = "OpenMapKitResponse";

    /**
     * Tracks how often users create shortcuts to forms
     */
    public static final String CREATE_SHORTCUT = "CreateShortcut";

    /**
     * Tracks how often instances that have been deleted on disk are opened for editing/viewing
     */
    public static final String OPEN_DELETED_INSTANCE = "OpenDeletedInstance";

    /**
     * Tracks how often users switch between projects
     */
    public static final String SWITCH_PROJECT = "ProjectSwitch";

    /**
     * Tracks how often users choose to try the demo project
     */
    public static final String TRY_DEMO = "ProjectCreateDemo";

    /**
     * Tracks how often projects are created using QR codes.
     **/
    public static final String QR_CREATE_PROJECT = "ProjectCreateQR";

    /**
     * Tracks how often projects are created by manually entering details.
     */
    public static final String MANUAL_CREATE_PROJECT = "ProjectCreateManual";

    /**
     * Tracks how often a Google account is used to configure a manually created project
     */
    public static final String GOOGLE_ACCOUNT_PROJECT = "ProjectCreateGoogle";

    /**
     * Tracks how often projects with the same connection settings as an existing one are attempted
     * to be created.
     */
    public static final String DUPLICATE_PROJECT = "ProjectCreateDuplicate";

    /**
     * Tracks how often users try to create projects with the same connection settings but then decide
     * to switch to an existing project instead. This will give us a sense of whether users are getting
     * confused about project identity and trying to recreate the same one multiple times.
     */
    public static final String DUPLICATE_PROJECT_SWITCH = "ProjectCreateDuplicateSwitch";

    /**
     * Tracks how often users delete projects
     */
    public static final String DELETE_PROJECT = "ProjectDelete";

    /**
     * These events track how often users change project display settings
     **/
    public static final String CHANGE_PROJECT_NAME = "ProjectChangeName";
    public static final String CHANGE_PROJECT_ICON = "ProjectChangeIcon";
    public static final String CHANGE_PROJECT_COLOR = "ProjectChangeColor";

    /**
     * Tracks how often users reconfigure a project using QR codes
     */
    public static final String RECONFIGURE_PROJECT = "ProjectReconfigure";

    public static final String FORMS_PROVIDER_QUERY = "FormsProviderQuery";
    public static final String FORMS_PROVIDER_INSERT = "FormsProviderInsert";
    public static final String FORMS_PROVIDER_UPDATE = "FormsProviderUpdate";
    public static final String FORMS_PROVIDER_DELETE = "FormsProviderDelete";

    public static final String INSTANCE_PROVIDER_QUERY = "InstanceProviderQuery";
    public static final String INSTANCE_PROVIDER_INSERT = "InstanceProviderInsert";
    public static final String INSTANCE_PROVIDER_UPDATE = "InstanceProviderUpdate";
    public static final String INSTANCE_PROVIDER_DELETE = "InstanceProviderDelete";

    /**
     * These track how often the external edit or view actions are used for forms or instances.
     * One event tracks when a project ID is included with the action URI and the other tracks when
     * it's not included.
     */
    public static final String FORM_ACTION_WITH_PROJECT_ID = "FormActionWithProjectId";
    public static final String FORM_ACTION_WITHOUT_PROJECT_ID = "FormActionWithoutProjectId";

    /**
     * Tracks how often an external edit or view action includes an extra we'd like to deprecate.
     */
    public static final String FORM_ACTION_WITH_FORM_MODE_EXTRA = "FormActionWithFormModeExtra";

    /**
     * Tracks how often the app needs to recreate the directory for the current project
     * when returning to or launching the app.
     */
    public static final String RECREATE_PROJECT_DIR = "RecreateProjectDir";
}
