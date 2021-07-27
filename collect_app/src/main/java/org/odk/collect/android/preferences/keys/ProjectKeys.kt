package org.odk.collect.android.preferences.keys

import com.google.android.gms.maps.GoogleMap
import com.mapbox.mapboxsdk.maps.Style
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.utilities.QuestionFontSizeUtils

object ProjectKeys {
    // server_preferences.xml
    const val KEY_PROTOCOL = "protocol"

    // odk_server_preferences.xml
    const val KEY_SERVER_URL = "server_url"
    const val KEY_USERNAME = "username"
    const val KEY_PASSWORD = "password"

    // custom_server_paths_preferences.xml
    const val KEY_FORMLIST_URL = "formlist_url"
    const val KEY_SUBMISSION_URL = "submission_url"

    // google_preferences.xml
    const val KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account"
    const val KEY_GOOGLE_SHEETS_URL = "google_sheets_url"

    // project_display.xml
    const val KEY_PROJECT_DISPLAY = "project_display"

    // user_interface_preferences.xml
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_LANGUAGE = "app_language"
    const val KEY_FONT_SIZE = "font_size"
    const val KEY_NAVIGATION = "navigation"
    const val KEY_SHOW_SPLASH = "showSplash"
    const val KEY_SPLASH_PATH = "splashPath"

    // map_preferences.xml
    const val KEY_BASEMAP_SOURCE = "basemap_source"

    // basemap styles
    const val KEY_GOOGLE_MAP_STYLE = "google_map_style"
    const val KEY_MAPBOX_MAP_STYLE = "mapbox_map_style"
    const val KEY_USGS_MAP_STYLE = "usgs_map_style"
    const val KEY_CARTO_MAP_STYLE = "carto_map_style"
    const val KEY_REFERENCE_LAYER = "reference_layer"

    // form_management_preferences.xml
    const val KEY_FORM_UPDATE_MODE = "form_update_mode"
    const val KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check"
    const val KEY_AUTOMATIC_UPDATE = "automatic_update"
    const val KEY_HIDE_OLD_FORM_VERSIONS = "hide_old_form_versions"
    const val KEY_AUTOSEND = "autosend"
    const val KEY_DELETE_AFTER_SEND = "delete_send"
    const val KEY_COMPLETED_DEFAULT = "default_completed"
    const val KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior"
    const val KEY_HIGH_RESOLUTION = "high_resolution"
    const val KEY_IMAGE_SIZE = "image_size"
    const val KEY_GUIDANCE_HINT = "guidance_hint"
    const val KEY_EXTERNAL_APP_RECORDING = "external_app_recording"
    const val KEY_INSTANCE_SYNC = "instance_sync"

    // identity_preferences.xml
    const val KEY_ANALYTICS = "analytics"

    // form_metadata_preferences.xml
    const val KEY_METADATA_USERNAME = "metadata_username"
    const val KEY_METADATA_PHONENUMBER = "metadata_phonenumber"
    const val KEY_METADATA_EMAIL = "metadata_email"
    const val KEY_FORM_METADATA = "form_metadata"
    const val KEY_BACKGROUND_LOCATION = "background_location"
    const val KEY_BACKGROUND_RECORDING = "background_recording"

    // values
    const val PROTOCOL_SERVER = "odk_default"
    const val PROTOCOL_GOOGLE_SHEETS = "google_sheets"
    const val NAVIGATION_SWIPE = "swipe"
    const val NAVIGATION_BUTTONS = "buttons"
    const val NAVIGATION_BOTH = "swipe_buttons"
    const val CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe"
    private const val AUTOSEND_OFF = "off"
    private const val GUIDANCE_HINT_OFF = "no"

    // basemap section
    const val CATEGORY_BASEMAP = "category_basemap"

    // basemap source values
    const val BASEMAP_SOURCE_GOOGLE = "google"
    const val BASEMAP_SOURCE_MAPBOX = "mapbox"
    const val BASEMAP_SOURCE_OSM = "osm"
    const val BASEMAP_SOURCE_USGS = "usgs"
    const val BASEMAP_SOURCE_STAMEN = "stamen"
    const val BASEMAP_SOURCE_CARTO = "carto"

    // experimental
    const val KEY_MAGENTA_THEME = "magenta"

    @JvmStatic
    val defaults: HashMap<String, Any>
        get() {
            val hashMap = HashMap<String, Any>()
            // odk_server_preferences.xml
            hashMap[KEY_SERVER_URL] = Collect.getInstance().getString(R.string.default_server_url)
            hashMap[KEY_USERNAME] = ""
            hashMap[KEY_PASSWORD] = ""
            // form_management_preferences.xml
            hashMap[KEY_AUTOSEND] = AUTOSEND_OFF
            hashMap[KEY_GUIDANCE_HINT] = GUIDANCE_HINT_OFF
            hashMap[KEY_DELETE_AFTER_SEND] = false
            hashMap[KEY_COMPLETED_DEFAULT] = true
            hashMap[KEY_CONSTRAINT_BEHAVIOR] = CONSTRAINT_BEHAVIOR_ON_SWIPE
            hashMap[KEY_HIGH_RESOLUTION] = true
            hashMap[KEY_IMAGE_SIZE] = "original_image_size"
            hashMap[KEY_INSTANCE_SYNC] = true
            hashMap[KEY_PERIODIC_FORM_UPDATES_CHECK] = "every_fifteen_minutes"
            hashMap[KEY_AUTOMATIC_UPDATE] = false
            hashMap[KEY_HIDE_OLD_FORM_VERSIONS] = true
            hashMap[KEY_BACKGROUND_LOCATION] = true
            hashMap[KEY_BACKGROUND_RECORDING] = true
            hashMap[KEY_FORM_UPDATE_MODE] = "manual"
            // form_metadata_preferences.xml
            hashMap[KEY_METADATA_USERNAME] = ""
            hashMap[KEY_METADATA_PHONENUMBER] = ""
            hashMap[KEY_METADATA_EMAIL] = ""
            // google_preferences.xml
            hashMap[KEY_SELECTED_GOOGLE_ACCOUNT] = ""
            hashMap[KEY_GOOGLE_SHEETS_URL] = ""
            // identity_preferences.xml
            hashMap[KEY_ANALYTICS] = true
            // custom_server_paths_preferenceshs_preferences.xml
            hashMap[KEY_FORMLIST_URL] =
                Collect.getInstance().getString(R.string.default_odk_formlist)
            hashMap[KEY_SUBMISSION_URL] =
                Collect.getInstance().getString(R.string.default_odk_submission)
            // server_preferences.xml
            hashMap[KEY_PROTOCOL] = PROTOCOL_SERVER
            // user_interface_preferences.xml
            hashMap[KEY_APP_THEME] = Collect.getInstance().getString(R.string.app_theme_light)
            hashMap[KEY_APP_LANGUAGE] = ""
            hashMap[KEY_FONT_SIZE] = QuestionFontSizeUtils.DEFAULT_FONT_SIZE.toString()
            hashMap[KEY_NAVIGATION] = NAVIGATION_BOTH
            hashMap[KEY_SHOW_SPLASH] = false
            hashMap[KEY_SPLASH_PATH] = Collect.getInstance().getString(R.string.default_splash_path)
            hashMap[KEY_MAGENTA_THEME] = false
            hashMap[KEY_EXTERNAL_APP_RECORDING] = true
            // map_preferences.xml
            hashMap[KEY_BASEMAP_SOURCE] = BASEMAP_SOURCE_GOOGLE
            hashMap[KEY_CARTO_MAP_STYLE] = "positron"
            hashMap[KEY_USGS_MAP_STYLE] = "topographic"
            hashMap[KEY_GOOGLE_MAP_STYLE] = GoogleMap.MAP_TYPE_NORMAL.toString()
            hashMap[KEY_MAPBOX_MAP_STYLE] = Style.MAPBOX_STREETS
            return hashMap
        }
}
