package org.odk.collect.android.preferences;

import java.util.Arrays;
import java.util.Collection;

public final class PreferenceKeys {
    public static final String KEY_LAST_VERSION             = "lastVersion";
    public static final String KEY_FIRST_RUN                = "firstRun";
    public static final String KEY_SHOW_SPLASH              = "showSplash";
    public static final String KEY_SPLASH_PATH              = "splashPath";
    public static final String KEY_FONT_SIZE                = "font_size";
    public static final String KEY_DELETE_AFTER_SEND        = "delete_send";
    public static final String KEY_ANALYTICS                = "analytics";
    public static final String KEY_INSTANCE_SYNC            = "instance_sync";
    public static final String KEY_APP_LANGUAGE             = "app_language";

    public static final String KEY_PROTOCOL                 = "protocol";
           static final String KEY_PROTOCOL_SETTINGS        = "protocol_settings";

    // leaving these in the main screen because username can be used as a
    // pre-fill value in a form
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT  = "selected_google_account";
    public static final String KEY_USERNAME                 = "username";
    public static final String KEY_PASSWORD                 = "password";

    // METADATA
    static final String KEY_FORM_METADATA                   = "form_metadata";
    /** Whether any existing username and email values have been migrated to form metadata */
    static final String KEY_METADATA_MIGRATED               = "metadata_migrated";
    public static final String KEY_METADATA_USERNAME        = "metadata_username";
    public static final String KEY_METADATA_PHONENUMBER     = "metadata_phonenumber";
    public static final String KEY_METADATA_EMAIL           = "metadata_email";

    // AGGREGATE SPECIFIC
    public static final String KEY_SERVER_URL               = "server_url";

    // GOOGLE SPECIFIC
    public static final String KEY_GOOGLE_SHEETS_URL        = "google_sheets_url";

    // OTHER SPECIFIC
    public static final String KEY_FORMLIST_URL             = "formlist_url";
    public static final String KEY_SUBMISSION_URL           = "submission_url";

    public static final String NAVIGATION_SWIPE             = "swipe";
    public static final String NAVIGATION_BUTTONS           = "buttons";

    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    public static final String CONSTRAINT_BEHAVIOR_DEFAULT  = "on_swipe";

    public static final String KEY_COMPLETED_DEFAULT        = "default_completed";

    public static final String KEY_HIGH_RESOLUTION          = "high_resolution";

    public static final String KEY_AUTOSEND_WIFI            = "autosend_wifi";
    public static final String KEY_AUTOSEND_NETWORK         = "autosend_network";

    public static final String KEY_NAVIGATION               = "navigation";
    public static final String KEY_CONSTRAINT_BEHAVIOR      = "constraint_behavior";

    // MAP SPECIFIC

    public static final String KEY_MAP_SDK                  = "map_sdk_behavior";
    public static final String KEY_MAP_BASEMAP              = "map_basemap_behavior";

           static final int    ARRAY_INDEX_GOOGLE_MAPS      = 0;
           static final String OSM_BASEMAP_KEY              = "osmdroid";
           static final String GOOGLE_MAPS_BASEMAP_DEFAULT  = "streets";
           static final String OSM_MAPS_BASEMAP_DEFAULT     = "mapquest_streets";

    /**
     * General keys
     */
    public static final Collection<String> ALL_GENERAL_KEYS = Arrays.asList(
            KEY_SHOW_SPLASH,
            KEY_SPLASH_PATH,
            KEY_FONT_SIZE,
            KEY_DELETE_AFTER_SEND,
            KEY_INSTANCE_SYNC,
            KEY_PROTOCOL,
            KEY_PROTOCOL_SETTINGS,
            KEY_SELECTED_GOOGLE_ACCOUNT,
            KEY_USERNAME,
            KEY_PASSWORD,
            KEY_FORM_METADATA,
            KEY_METADATA_USERNAME,
            KEY_METADATA_PHONENUMBER,
            KEY_METADATA_EMAIL,
            KEY_SERVER_URL,
            KEY_GOOGLE_SHEETS_URL,
            KEY_FORMLIST_URL,
            KEY_SUBMISSION_URL,
            KEY_NAVIGATION,
            KEY_CONSTRAINT_BEHAVIOR,
            KEY_COMPLETED_DEFAULT,
            KEY_MAP_SDK,
            KEY_MAP_BASEMAP,
            OSM_BASEMAP_KEY,
            GOOGLE_MAPS_BASEMAP_DEFAULT,
            OSM_MAPS_BASEMAP_DEFAULT
    );
}
