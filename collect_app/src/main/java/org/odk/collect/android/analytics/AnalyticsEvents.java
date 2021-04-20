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
     * Track submissions to a URL with a custom submission endpoint configured in settings. The action
     * should be a hash of the endpoint setting.
     */
    public static final String CUSTOM_ENDPOINT_SUB = "CustomEndpointSub";

    /**
     * Track usage of legacy Aggregate < 1 form list API code paths.
     */
    public static final String LEGACY_FORM_LIST = "LegacyFormList";

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
     * Tracks how often people see the URL question
     */
    public static final String URL_QUESTION = "UrlQuestion";

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
     * from disk and then encrypted
     */
    public static final String IMPORT_AND_ENCRYPT_INSTANCE = "ImportAndEncryptInstance";
}
