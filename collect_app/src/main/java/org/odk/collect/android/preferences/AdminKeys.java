package org.odk.collect.android.preferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AdminKeys {
    // key for this preference screen
    public static String KEY_ADMIN_PW = "admin_pw";

    // keys for each preference
    // main menu
    public static String KEY_EDIT_SAVED = "edit_saved";
    public static String KEY_SEND_FINALIZED = "send_finalized";
    public static String KEY_VIEW_SENT = "view_sent";
    public static String KEY_GET_BLANK = "get_blank";
    public static String KEY_DELETE_SAVED = "delete_saved";
    // form entry
    public static String KEY_SAVE_MID = "save_mid";
    public static String KEY_JUMP_TO = "jump_to";
    public static String KEY_CHANGE_LANGUAGE = "change_language";
    public static String KEY_ACCESS_SETTINGS = "access_settings";
    public static String KEY_SAVE_AS = "save_as";
    public static String KEY_MARK_AS_FINALIZED = "mark_as_finalized";
    // server
    static String KEY_CHANGE_SERVER = "change_server";
    static String KEY_CHANGE_USERNAME = "change_username";
    static String KEY_CHANGE_PASSWORD = "change_password";
    static String KEY_CHANGE_ADMIN_PASSWORD = "admin_password";
    static String KEY_CHANGE_GOOGLE_ACCOUNT = "change_google_account";
    static String KEY_CHANGE_PROTOCOL_SETTINGS = "change_protocol_settings";
    // client
    static String KEY_CHANGE_FONT_SIZE = "change_font_size";
    static String KEY_DEFAULT_TO_FINALIZED = "default_to_finalized";
    static String KEY_HIGH_RESOLUTION = "high_resolution";
    static String KEY_SHOW_SPLASH_SCREEN = "show_splash_screen";
    static String KEY_DELETE_AFTER_SEND = "delete_after_send";

    static String KEY_AUTOSEND_WIFI = "autosend_wifi";
    static String KEY_AUTOSEND_NETWORK = "autosend_network";

    static String KEY_NAVIGATION = "navigation";
    static String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

    static String KEY_FORM_PROCESSING_LOGIC = "form_processing_logic";

    static String KEY_SHOW_MAP_SDK = "show_map_sdk";
    static String KEY_SHOW_MAP_BASEMAP = "show_map_basemap";

    static String KEY_ANALYTICS = "analytics";

    /** A map of admin keys to the general keys they enable */
    static Map<String, String> adminToGeneral = Collections.unmodifiableMap(
            new HashMap<String, String>() {{
                put(KEY_AUTOSEND_WIFI,              PreferenceKeys.KEY_AUTOSEND_WIFI);
                put(KEY_AUTOSEND_NETWORK,           PreferenceKeys.KEY_AUTOSEND_NETWORK);
                put(KEY_CHANGE_SERVER,              PreferenceKeys.KEY_PROTOCOL);
                put(KEY_CHANGE_PROTOCOL_SETTINGS,   PreferenceKeys.KEY_PROTOCOL_SETTINGS);
                put(KEY_DEFAULT_TO_FINALIZED,       PreferenceKeys.KEY_COMPLETED_DEFAULT);
                put(KEY_DELETE_AFTER_SEND,          PreferenceKeys.KEY_DELETE_AFTER_SEND);
                put(KEY_HIGH_RESOLUTION,            PreferenceKeys.KEY_HIGH_RESOLUTION);
                put(KEY_ANALYTICS,                  PreferenceKeys.KEY_ANALYTICS);
                put(KEY_SHOW_SPLASH_SCREEN,         PreferenceKeys.KEY_SPLASH_PATH);
                put(KEY_CHANGE_FONT_SIZE,           PreferenceKeys.KEY_FONT_SIZE);
                put(KEY_CONSTRAINT_BEHAVIOR,        PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR);
                put(KEY_SHOW_MAP_SDK,               PreferenceKeys.KEY_MAP_SDK);
                put(KEY_SHOW_MAP_BASEMAP,           PreferenceKeys.KEY_MAP_BASEMAP);
                put(KEY_NAVIGATION,                 PreferenceKeys.KEY_NAVIGATION);
                put(KEY_CHANGE_PASSWORD,            PreferenceKeys.KEY_PASSWORD);
                put(KEY_CHANGE_USERNAME,            PreferenceKeys.KEY_USERNAME);
                put(KEY_CHANGE_GOOGLE_ACCOUNT,      PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
            }});
}
