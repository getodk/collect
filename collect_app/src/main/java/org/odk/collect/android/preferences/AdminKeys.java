package org.odk.collect.android.preferences;

import static org.odk.collect.android.preferences.AdminAndGeneralKeys.ag;

public final class AdminKeys {
    // key for this preference screen
   final public static String KEY_ADMIN_PW = "admin_pw";

    // keys for each preference

    // main menu
    final public  static String KEY_EDIT_SAVED                = "edit_saved";
    final public  static String KEY_SEND_FINALIZED            = "send_finalized";
    final public  static String KEY_VIEW_SENT                 = "view_sent";
    final public  static String KEY_GET_BLANK                 = "get_blank";
    final public  static String KEY_DELETE_SAVED              = "delete_saved";

    // form entry
    final public  static String KEY_SAVE_MID                  = "save_mid";
    final public  static String KEY_JUMP_TO                   = "jump_to";
    final public  static String KEY_CHANGE_LANGUAGE           = "change_language";
    final public  static String KEY_ACCESS_SETTINGS           = "access_settings";
    final public  static String KEY_SAVE_AS                   = "save_as";
    final public  static String KEY_MARK_AS_FINALIZED         = "mark_as_finalized";

    // server
    final static String KEY_CHANGE_ADMIN_PASSWORD     = "admin_password";
    final static String KEY_CHANGE_GOOGLE_ACCOUNT     = "change_google_account";
    final private static String KEY_CHANGE_SERVER             = "change_server";
    final private static String KEY_CHANGE_USERNAME           = "change_username";
    final private static String KEY_CHANGE_PASSWORD           = "change_password";
    final private static String KEY_CHANGE_PROTOCOL_SETTINGS  = "change_protocol_settings";

    // client
    final static String KEY_FORM_PROCESSING_LOGIC     = "form_processing_logic";

    final private static String KEY_CHANGE_FONT_SIZE          = "change_font_size";
    final private static String KEY_DEFAULT_TO_FINALIZED      = "default_to_finalized";
    final private static String KEY_HIGH_RESOLUTION           = "high_resolution";
    final private static String KEY_SHOW_SPLASH_SCREEN        = "show_splash_screen";
    final private static String KEY_DELETE_AFTER_SEND         = "delete_after_send";
    final private static String KEY_INSTANCE_FORM_SYNC        = "instance_form_sync";

    final private static String KEY_AUTOSEND_WIFI             = "autosend_wifi";
    final private static String KEY_AUTOSEND_NETWORK          = "autosend_network";

    final private static String KEY_NAVIGATION                = "navigation";
    final private static String KEY_CONSTRAINT_BEHAVIOR       = "constraint_behavior";

    final private static String KEY_SHOW_MAP_SDK              = "show_map_sdk";
    final private static String KEY_SHOW_MAP_BASEMAP          = "show_map_basemap";

    final private static String KEY_ANALYTICS                 = "analytics";

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
        ag(KEY_CHANGE_PASSWORD,            PreferenceKeys.KEY_PASSWORD),
        ag(KEY_CHANGE_USERNAME,            PreferenceKeys.KEY_USERNAME),
        ag(KEY_CHANGE_GOOGLE_ACCOUNT,      PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT),
        ag(KEY_INSTANCE_FORM_SYNC,         PreferenceKeys.KEY_INSTANCE_SYNC)
    };
}
