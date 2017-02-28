package org.odk.collect.android.preferences;

public final class PreferenceKeys {
    public static final String KEY_LAST_VERSION = "lastVersion";
    public static final String KEY_FIRST_RUN = "firstRun";
    public static final String KEY_SHOW_SPLASH = "showSplash";
    public static final String KEY_SPLASH_PATH = "splashPath";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_DELETE_AFTER_SEND = "delete_send";
    public static final String KEY_ANALYTICS = "analytics";

    public static final String KEY_PROTOCOL = "protocol";
           static final String KEY_PROTOCOL_SETTINGS = "protocol_settings";

    // leaving these in the main screen because username can be used as adminKey
    // pre-fill
    // value in adminKey form
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    // AGGREGATE SPECIFIC
    public static final String KEY_SERVER_URL = "server_url";

    // GOOGLE SPECIFIC
    public static final String KEY_GOOGLE_SHEETS_URL = "google_sheets_url";

    // OTHER SPECIFIC
    public static final String KEY_FORMLIST_URL = "formlist_url";
    public static final String KEY_SUBMISSION_URL = "submission_url";

    public static final String NAVIGATION_SWIPE = "swipe";
    public static final String NAVIGATION_BUTTONS = "buttons";

    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    public static final String CONSTRAINT_BEHAVIOR_DEFAULT = "on_swipe";

    public static final String KEY_COMPLETED_DEFAULT = "default_completed";

    public static final String KEY_HIGH_RESOLUTION = "high_resolution";

    public static final String KEY_AUTOSEND_WIFI = "autosend_wifi";
    public static final String KEY_AUTOSEND_NETWORK = "autosend_network";

    public static final String KEY_NAVIGATION = "navigation";
    public static final String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

    // MAP SPECIFIC

    public static final String KEY_MAP_SDK = "map_sdk_behavior";
    public static final String KEY_MAP_BASEMAP = "map_basemap_behavior";

           static final int ARRAY_INDEX_GOOGLE_MAPS = 0;
           static final String OSM_BASEMAP_KEY = "osmdroid";
           static final String GOOGLE_MAPS_BASEMAP_DEFAULT = "streets";
           static final String OSM_MAPS_BASEMAP_DEFAULT = "mapquest_streets";
}
