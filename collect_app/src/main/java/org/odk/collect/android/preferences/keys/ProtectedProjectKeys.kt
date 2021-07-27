package org.odk.collect.android.preferences.keys

object ProtectedProjectKeys {
    const val KEY_ADMIN_PW = "admin_pw"

    // Main Menu Settings
    const val KEY_EDIT_SAVED = "edit_saved"
    const val KEY_SEND_FINALIZED = "send_finalized"
    const val KEY_VIEW_SENT = "view_sent"
    const val KEY_GET_BLANK = "get_blank"
    const val KEY_DELETE_SAVED = "delete_saved"

    // User Settings
    const val KEY_CHANGE_SERVER = "change_server"
    const val KEY_CHANGE_PROJECT_DISPLAY = "change_project_display"
    const val KEY_APP_THEME = "change_app_theme"
    const val KEY_APP_LANGUAGE = "change_app_language"
    const val KEY_CHANGE_FONT_SIZE = "change_font_size"
    const val KEY_NAVIGATION = "change_navigation"
    const val KEY_SHOW_SPLASH_SCREEN = "show_splash_screen"
    const val KEY_MAPS = "maps"
    const val KEY_FORM_UPDATE_MODE = "form_update_mode"
    const val KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check"
    const val KEY_AUTOMATIC_UPDATE = "automatic_update"
    const val KEY_HIDE_OLD_FORM_VERSIONS = "hide_old_form_versions"
    const val KEY_AUTOSEND = "change_autosend"
    const val KEY_DELETE_AFTER_SEND = "delete_after_send"
    const val KEY_DEFAULT_TO_FINALIZED = "default_to_finalized"
    const val KEY_CONSTRAINT_BEHAVIOR = "change_constraint_behavior"
    const val KEY_HIGH_RESOLUTION = "high_resolution"
    const val KEY_IMAGE_SIZE = "image_size"
    const val KEY_GUIDANCE_HINT = "guidance_hint"
    const val KEY_EXTERNAL_APP_RECORDING = "external_app_recording"
    const val KEY_INSTANCE_FORM_SYNC = "instance_form_sync"
    const val KEY_CHANGE_FORM_METADATA = "change_form_metadata"
    const val KEY_ANALYTICS = "analytics"

    // Form Entry Settings
    const val KEY_MOVING_BACKWARDS = "moving_backwards"
    const val KEY_ACCESS_SETTINGS = "access_settings"
    const val KEY_CHANGE_LANGUAGE = "change_language"
    const val KEY_JUMP_TO = "jump_to"
    const val KEY_SAVE_MID = "save_mid"
    const val KEY_SAVE_AS = "save_as"
    const val KEY_MARK_AS_FINALIZED = "mark_as_finalized"

    const val ALLOW_OTHER_WAYS_OF_EDITING_FORM = "allow_other_ways_of_editing_form"

    @JvmStatic
    val defaults: Map<String, Any>
        get() {
            val defaults: MutableMap<String, Any> = HashMap()
            for (key in allKeys()) {
                if (key == KEY_ADMIN_PW) {
                    defaults[key] = ""
                } else {
                    defaults[key] = true
                }
            }
            return defaults
        }

    fun allKeys() = listOf(
        KEY_ADMIN_PW,

        KEY_EDIT_SAVED,
        KEY_SEND_FINALIZED,
        KEY_VIEW_SENT,
        KEY_GET_BLANK,
        KEY_DELETE_SAVED,

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

        KEY_MOVING_BACKWARDS,
        KEY_ACCESS_SETTINGS,
        KEY_CHANGE_LANGUAGE,
        KEY_JUMP_TO,
        KEY_SAVE_MID,
        KEY_SAVE_AS,
        KEY_MARK_AS_FINALIZED,
        ALLOW_OTHER_WAYS_OF_EDITING_FORM
    )
}
