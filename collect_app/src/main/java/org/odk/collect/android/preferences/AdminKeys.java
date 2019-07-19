package org.odk.collect.android.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.odk.collect.android.preferences.AdminAndGeneralKeys.ag;

/** Admin preference settings keys. The values match those of the keys in admin_preferences.xml. */
public final class AdminKeys {
    // NOTE TO MAINTAINERS: ensure all keys defined below are in adminToGeneral or
    // otherKeys below, for automated testing.

    // key for this preference screen
    public static final String KEY_ADMIN_PW                     = "admin_pw";

    // keys for each preference

    // main menu
    public static final String KEY_EDIT_SAVED                   = "edit_saved";
    public static final String KEY_SEND_FINALIZED               = "send_finalized";
    public static final String KEY_VIEW_SENT                    = "view_sent";
    public static final String KEY_GET_BLANK                    = "get_blank";
    public static final String KEY_DELETE_SAVED                 = "delete_saved";

    // form entry
    public static final String KEY_SAVE_MID                     = "save_mid";
    public static final String KEY_JUMP_TO                      = "jump_to";
    public static final String KEY_CHANGE_LANGUAGE              = "change_language";
    public static final String KEY_ACCESS_SETTINGS              = "access_settings";
    public static final String KEY_SAVE_AS                      = "save_as";
    public static final String KEY_MARK_AS_FINALIZED            = "mark_as_finalized";

    // server
    static final String KEY_CHANGE_ADMIN_PASSWORD               = "admin_password";
    static final String KEY_IMPORT_SETTINGS                     = "import_settings";
    private static final String KEY_CHANGE_SERVER               = "change_server";
    private static final String KEY_CHANGE_SUBMISSION_TRANSPORT = "change_submission_transport";
    private static final String KEY_CHANGE_FORM_METADATA        = "change_form_metadata";

    // client
    private static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    private static final String KEY_AUTOMATIC_UPDATE            = "automatic_update";
    private static final String KEY_HIDE_OLD_FORM_VERSIONS      = "hide_old_form_versions";
    private static final String KEY_CHANGE_FONT_SIZE            = "change_font_size";
    private static final String KEY_DEFAULT_TO_FINALIZED        = "default_to_finalized";
    private static final String KEY_HIGH_RESOLUTION             = "high_resolution";
    private static final String KEY_IMAGE_SIZE                  = "image_size";
    private static final String KEY_GUIDANCE_HINT               = "guidance_hint";
    private static final String KEY_SHOW_SPLASH_SCREEN          = "show_splash_screen";
    private static final String KEY_DELETE_AFTER_SEND           = "delete_after_send";
    private static final String KEY_INSTANCE_FORM_SYNC          = "instance_form_sync";
    private static final String KEY_APP_LANGUAGE                = "change_app_language";
    private static final String KEY_APP_THEME                   = "change_app_theme";

    private static final String KEY_AUTOSEND                    = "change_autosend";

    private static final String KEY_NAVIGATION                  = "change_navigation";
    static final String KEY_CONSTRAINT_BEHAVIOR                 = "change_constraint_behavior";

    private static final String KEY_BASE_LAYER                  = "base_layer";

    private static final String KEY_ANALYTICS                   = "analytics";
    public static final String KEY_MOVING_BACKWARDS             = "moving_backwards";
    static final String ALLOW_OTHER_WAYS_OF_EDITING_FORM        = "allow_other_ways_of_editing_form";

    /**
     * The admin preferences allow removing general preferences. This array contains
     * tuples of admin keys and the keys of general preferences that are removed if the admin
     * preference is false.
     */
    static AdminAndGeneralKeys[] adminToGeneral = new AdminAndGeneralKeys[] {

            ag(KEY_CHANGE_SERVER,              GeneralKeys.KEY_PROTOCOL),
            ag(KEY_CHANGE_SUBMISSION_TRANSPORT, GeneralKeys.KEY_TRANSPORT_PREFERENCE),
            ag(KEY_CHANGE_SUBMISSION_TRANSPORT, GeneralKeys.KEY_SMS_PREFERENCE),
            ag(KEY_CHANGE_FORM_METADATA,       GeneralKeys.KEY_FORM_METADATA),

            ag(KEY_PERIODIC_FORM_UPDATES_CHECK, GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK),
            ag(KEY_AUTOMATIC_UPDATE,           GeneralKeys.KEY_AUTOMATIC_UPDATE),
            ag(KEY_HIDE_OLD_FORM_VERSIONS,     GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS),
            ag(KEY_CHANGE_FONT_SIZE,           GeneralKeys.KEY_FONT_SIZE),
            ag(KEY_APP_LANGUAGE,               GeneralKeys.KEY_APP_LANGUAGE),
            ag(KEY_DEFAULT_TO_FINALIZED,       GeneralKeys.KEY_COMPLETED_DEFAULT),
            ag(KEY_HIGH_RESOLUTION,            GeneralKeys.KEY_HIGH_RESOLUTION),
            ag(KEY_IMAGE_SIZE,                 GeneralKeys.KEY_IMAGE_SIZE),
            ag(KEY_GUIDANCE_HINT,              GeneralKeys.KEY_GUIDANCE_HINT),
            ag(KEY_SHOW_SPLASH_SCREEN,         GeneralKeys.KEY_SHOW_SPLASH),
            ag(KEY_SHOW_SPLASH_SCREEN,         GeneralKeys.KEY_SPLASH_PATH),
            ag(KEY_DELETE_AFTER_SEND,          GeneralKeys.KEY_DELETE_AFTER_SEND),
            ag(KEY_INSTANCE_FORM_SYNC,         GeneralKeys.KEY_INSTANCE_SYNC),
            ag(KEY_APP_THEME,                  GeneralKeys.KEY_APP_THEME),

            ag(KEY_AUTOSEND,                   GeneralKeys.KEY_AUTOSEND),

            ag(KEY_NAVIGATION,                 GeneralKeys.KEY_NAVIGATION),
            ag(KEY_CONSTRAINT_BEHAVIOR,        GeneralKeys.KEY_CONSTRAINT_BEHAVIOR),

            ag(KEY_BASE_LAYER,                 GeneralKeys.KEY_BASE_LAYER_SOURCE),

            ag(KEY_ANALYTICS,                  GeneralKeys.KEY_ANALYTICS)
    };

    /** Admin keys other than those in adminToGeneral above */
    private static Collection<String> otherKeys = Arrays.asList(
            KEY_EDIT_SAVED,
            KEY_SEND_FINALIZED,
            KEY_VIEW_SENT,
            KEY_GET_BLANK,
            KEY_DELETE_SAVED,
            KEY_SAVE_MID,
            KEY_JUMP_TO,
            KEY_CHANGE_LANGUAGE,
            KEY_ACCESS_SETTINGS,
            KEY_SAVE_AS,
            KEY_MARK_AS_FINALIZED,
            KEY_CHANGE_ADMIN_PASSWORD,
            KEY_MOVING_BACKWARDS,
            ALLOW_OTHER_WAYS_OF_EDITING_FORM
    );

    static Collection<String> serverKeys = Collections.singletonList(
            KEY_CHANGE_SERVER
    );

    static Collection<String> identityKeys = Arrays.asList(
            KEY_CHANGE_FORM_METADATA,
            KEY_ANALYTICS
    );

    static Collection<String> formManagementKeys = Arrays.asList(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            KEY_AUTOMATIC_UPDATE,
            KEY_HIDE_OLD_FORM_VERSIONS,
            KEY_AUTOSEND,
            KEY_DELETE_AFTER_SEND,
            KEY_DEFAULT_TO_FINALIZED,
            KEY_CONSTRAINT_BEHAVIOR,
            KEY_HIGH_RESOLUTION,
            KEY_IMAGE_SIZE,
            KEY_GUIDANCE_HINT,
            KEY_INSTANCE_FORM_SYNC
    );

    static Collection<String> userInterfaceKeys = Arrays.asList(
            KEY_APP_THEME,
            KEY_APP_LANGUAGE,
            KEY_CHANGE_FONT_SIZE,
            KEY_NAVIGATION,
            KEY_SHOW_SPLASH_SCREEN
    );

    static Collection<String> mapsKeys = Arrays.asList(
            KEY_BASE_LAYER
    );

    private static Collection<String> allKeys() {
        Collection<String> keys = new ArrayList<>();
        for (AdminAndGeneralKeys atg : adminToGeneral) {
            keys.add(atg.adminKey);
        }
        for (String key : otherKeys) {
            keys.add(key);
        }
        return keys;
    }

    public static final Collection<String> ALL_KEYS = allKeys();

    private AdminKeys() {

    }
}
