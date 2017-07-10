package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.HashMap;

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

    public static final String KEY_AUTOSEND                 = "autosend";
    public static final String KEY_AUTOSEND_WIFI            = "autosend_wifi";
    public static final String KEY_AUTOSEND_NETWORK         = "autosend_network";

    public static final String KEY_TIMER_LOG_ENABLED        = "timer_log";

    public static final String KEY_NAVIGATION               = "navigation";
    public static final String KEY_CONSTRAINT_BEHAVIOR      = "constraint_behavior";

    // MAP SPECIFIC

    public static final String KEY_MAP_SDK                  = "map_sdk_behavior";
    public static final String KEY_MAP_BASEMAP              = "map_basemap_behavior";

           static final int    ARRAY_INDEX_GOOGLE_MAPS      = 0;
           static final String OSM_BASEMAP_KEY              = "osmdroid";
           static final String GOOGLE_MAPS_BASEMAP_DEFAULT  = "streets";
           static final String OSM_MAPS_BASEMAP_DEFAULT     = "mapquest_streets";

    private static HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put(KEY_SHOW_SPLASH,                false);
        hashMap.put(KEY_SPLASH_PATH,                Collect.getInstance().getString(R.string.default_splash_path));
        hashMap.put(KEY_FONT_SIZE,                  "21");
        hashMap.put(KEY_DELETE_AFTER_SEND,          false);
        hashMap.put(KEY_ANALYTICS,                  true);
        hashMap.put(KEY_INSTANCE_SYNC,              true);
        hashMap.put(KEY_APP_LANGUAGE,               "");
        hashMap.put(KEY_PROTOCOL,                   Collect.getInstance().getString(R.string.protocol_odk_default));
        hashMap.put(KEY_SELECTED_GOOGLE_ACCOUNT,    "");
        hashMap.put(KEY_USERNAME,                   "");
        hashMap.put(KEY_METADATA_USERNAME,          "");
        hashMap.put(KEY_METADATA_PHONENUMBER,       "");
        hashMap.put(KEY_METADATA_EMAIL,             "");
        hashMap.put(KEY_SERVER_URL,                 Collect.getInstance().getString(R.string.default_server_url));
        hashMap.put(KEY_GOOGLE_SHEETS_URL,          "");
        hashMap.put(KEY_FORMLIST_URL,               Collect.getInstance().getString(R.string.default_odk_formlist));
        hashMap.put(KEY_SUBMISSION_URL,             Collect.getInstance().getString(R.string.default_odk_submission));
        hashMap.put(KEY_NAVIGATION,                 "swipe");
        hashMap.put(KEY_CONSTRAINT_BEHAVIOR,        "on_swipe");
        hashMap.put(KEY_COMPLETED_DEFAULT,          true);
        hashMap.put(KEY_MAP_SDK,                    "google_maps");
        hashMap.put(KEY_MAP_BASEMAP,                "streets");
        hashMap.put(KEY_AUTOSEND,                   "off");
        return hashMap;
    }

    public static final HashMap<String, Object> GENERAL_KEYS = getHashMap();

}
