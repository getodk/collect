package org.odk.collect.android.preferences.keys;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Protected project preference settings keys. */
public final class ProtectedProjectKeys {
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

    public static final String KEY_CHANGE_SERVER               = "change_server";
    public static final String KEY_CHANGE_PROJECT_DISPLAY      = "change_project_display";
    public static final String KEY_APP_THEME                   = "change_app_theme";
    public static final String KEY_APP_LANGUAGE                = "change_app_language";
    public static final String KEY_CHANGE_FONT_SIZE            = "change_font_size";
    public static final String KEY_NAVIGATION                  = "change_navigation";
    public static final String KEY_SHOW_SPLASH_SCREEN          = "show_splash_screen";
    public static final String KEY_EXTERNAL_APP_RECORDING      = "external_app_recording";

    public static final String KEY_MAPS                                = "maps";

    public static final String KEY_FORM_UPDATE_MODE            = "form_update_mode";
    public static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    public static final String KEY_AUTOMATIC_UPDATE            = "automatic_update";
    public static final String KEY_HIDE_OLD_FORM_VERSIONS      = "hide_old_form_versions";
    public static final String KEY_AUTOSEND                    = "change_autosend";
    public static final String KEY_DELETE_AFTER_SEND           = "delete_after_send";
    public static final String KEY_DEFAULT_TO_FINALIZED        = "default_to_finalized";
    public static final String KEY_CONSTRAINT_BEHAVIOR         = "change_constraint_behavior";
    public static final String KEY_HIGH_RESOLUTION             = "high_resolution";
    public static final String KEY_IMAGE_SIZE                  = "image_size";
    public static final String KEY_GUIDANCE_HINT               = "guidance_hint";
    public static final String KEY_INSTANCE_FORM_SYNC          = "instance_form_sync";
    public static final String KEY_CHANGE_FORM_METADATA        = "change_form_metadata";
    public static final String KEY_ANALYTICS                   = "analytics";

    public static final String KEY_MOVING_BACKWARDS             = "moving_backwards";
    public static final String KEY_ACCESS_SETTINGS              = "access_settings";
    public static final String KEY_CHANGE_LANGUAGE              = "change_language";
    public static final String KEY_JUMP_TO                      = "jump_to";
    public static final String KEY_SAVE_MID                     = "save_mid";
    public static final String KEY_SAVE_AS                      = "save_as";
    public static final String KEY_MARK_AS_FINALIZED            = "mark_as_finalized";

    public static final String KEY_CHANGE_ADMIN_PASSWORD               = "admin_password";
    public static final String KEY_IMPORT_SETTINGS                     = "import_settings";
    public static final String ALLOW_OTHER_WAYS_OF_EDITING_FORM        = "allow_other_ways_of_editing_form";

    public static Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();

        for (String key : allKeys()) {
            if (key.equals(KEY_ADMIN_PW)) {
                defaults.put(key, "");
            } else {
                defaults.put(key, true);
            }
        }

        return defaults;
    }

    public static Collection<String> allKeys() {
        return Arrays.asList(
                KEY_CHANGE_SERVER,
                KEY_CHANGE_PROJECT_DISPLAY,
                KEY_APP_THEME,
                KEY_APP_LANGUAGE,
                KEY_CHANGE_FONT_SIZE,
                KEY_NAVIGATION,
                KEY_SHOW_SPLASH_SCREEN,
                KEY_MAPS,
                KEY_FORM_UPDATE_MODE,
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
                KEY_EXTERNAL_APP_RECORDING,
                KEY_INSTANCE_FORM_SYNC,
                KEY_CHANGE_FORM_METADATA,
                KEY_ANALYTICS,
                KEY_EDIT_SAVED,
                KEY_SEND_FINALIZED,
                KEY_VIEW_SENT,
                KEY_GET_BLANK,
                KEY_DELETE_SAVED,
                KEY_MOVING_BACKWARDS,
                KEY_ACCESS_SETTINGS,
                KEY_CHANGE_LANGUAGE,
                KEY_JUMP_TO,
                KEY_SAVE_MID,
                KEY_SAVE_AS,
                KEY_MARK_AS_FINALIZED,
                KEY_CHANGE_ADMIN_PASSWORD,
                ALLOW_OTHER_WAYS_OF_EDITING_FORM,
                KEY_ADMIN_PW
        );
    }

    private ProtectedProjectKeys() {

    }
}
