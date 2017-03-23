package org.odk.collect.android.preferences;

import static org.odk.collect.android.preferences.AdminAndGeneralKeys.ag;

public final class AdminKeys {
    // key for this preference screen
    public static final String KEY_ADMIN_PW = "admin_pw";

    // keys for each preference

    // main menu
    public static final String KEY_EDIT_SAVED           = "edit_saved";
    public static final String KEY_SEND_FINALIZED       = "send_finalized";
    public static final String KEY_VIEW_SENT            = "view_sent";
    public static final String KEY_GET_BLANK            = "get_blank";
    public static final String KEY_DELETE_SAVED         = "delete_saved";

    // form entry
    public static final String KEY_SAVE_MID             = "save_mid";
    public static final String KEY_JUMP_TO              = "jump_to";
    public static final String KEY_CHANGE_LANGUAGE      = "change_language";
    public static final String KEY_ACCESS_SETTINGS      = "access_settings";
    public static final String KEY_SAVE_AS              = "save_as";
    public static final String KEY_MARK_AS_FINALIZED    = "mark_as_finalized";

    // server
    static final String KEY_CHANGE_ADMIN_PASSWORD       = "admin_password";
    static final String KEY_CHANGE_GOOGLE_ACCOUNT       = "change_google_account";
    static final String KEY_CHANGE_SERVER               = "change_server";
    static final String KEY_CHANGE_PROTOCOL_SETTINGS    = "change_protocol_settings";

    // client
    static final String KEY_FORM_PROCESSING_LOGIC       = "form_processing_logic";

    static final String KEY_CHANGE_FONT_SIZE            = "change_font_size";
    static final String KEY_DEFAULT_TO_FINALIZED        = "default_to_finalized";
    static final String KEY_HIGH_RESOLUTION             = "high_resolution";
    static final String KEY_SHOW_SPLASH_SCREEN          = "show_splash_screen";
    static final String KEY_DELETE_AFTER_SEND           = "delete_after_send";
    static final String KEY_INSTANCE_FORM_SYNC          = "instance_form_sync";

    static final String KEY_AUTOSEND_WIFI               = "autosend_wifi";
    static final String KEY_AUTOSEND_NETWORK            = "autosend_network";

    static final String KEY_NAVIGATION                  = "navigation";
    static final String KEY_CONSTRAINT_BEHAVIOR         = "constraint_behavior";

    static final String KEY_SHOW_MAP_SDK                = "show_map_sdk";
    static final String KEY_SHOW_MAP_BASEMAP            = "show_map_basemap";

    static final String KEY_ANALYTICS                   = "analytics";

    /**
     * The admin preferences allow removing general preferences. This array contains
     * tuples of admin keys and the keys of general preferences that are removed if the admin
     * preference is false.
     */
    static AdminAndGeneralKeys[] adminToGeneral = new AdminAndGeneralKeys[] {
        ag(KEY_CHANGE_SERVER,              PreferenceKeys.KEY_PROTOCOL),
        ag(KEY_CHANGE_PROTOCOL_SETTINGS,   PreferenceKeys.KEY_PROTOCOL_SETTINGS),
        ag(KEY_AUTOSEND_WIFI,              PreferenceKeys.KEY_AUTOSEND_WIFI),
        ag(KEY_AUTOSEND_NETWORK,           PreferenceKeys.KEY_AUTOSEND_NETWORK),
        ag(KEY_DEFAULT_TO_FINALIZED,       PreferenceKeys.KEY_COMPLETED_DEFAULT),
        ag(KEY_DELETE_AFTER_SEND,          PreferenceKeys.KEY_DELETE_AFTER_SEND),
        ag(KEY_HIGH_RESOLUTION,            PreferenceKeys.KEY_HIGH_RESOLUTION),
        ag(KEY_ANALYTICS,                  PreferenceKeys.KEY_ANALYTICS),
        ag(KEY_SHOW_SPLASH_SCREEN,         PreferenceKeys.KEY_SHOW_SPLASH),
        ag(KEY_SHOW_SPLASH_SCREEN,         PreferenceKeys.KEY_SPLASH_PATH),
        ag(KEY_CHANGE_FONT_SIZE,           PreferenceKeys.KEY_FONT_SIZE),
        ag(KEY_CONSTRAINT_BEHAVIOR,        PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR),
        ag(KEY_SHOW_MAP_SDK,               PreferenceKeys.KEY_MAP_SDK),
        ag(KEY_SHOW_MAP_BASEMAP,           PreferenceKeys.KEY_MAP_BASEMAP),
        ag(KEY_NAVIGATION,                 PreferenceKeys.KEY_NAVIGATION),
        ag(KEY_INSTANCE_FORM_SYNC,         PreferenceKeys.KEY_INSTANCE_SYNC)
    };
}
