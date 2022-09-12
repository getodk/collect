package org.odk.collect.android.preferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;

import java.util.HashMap;

public final class GeneralKeys {
    // server_preferences.xml
    public static final String KEY_PROTOCOL = "protocol";

    // odk_server_preferences.xmll
    public static final String KEY_SERVER_URL = "server_url";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    // custom_server_paths_preferences.xmlreferences.xml
    public static final String KEY_FORMLIST_URL = "formlist_url";
    public static final String KEY_SUBMISSION_URL = "submission_url";

    // google_preferences.xml
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account";
    public static final String KEY_GOOGLE_SHEETS_URL = "google_sheets_url";

    // user_interface_preferences.xml
    public static final String KEY_APP_THEME = "appTheme";
    public static final String KEY_APP_LANGUAGE = "app_language";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_NAVIGATION = "navigation";
    public static final String KEY_SHOW_SPLASH = "showSplash";
    public static final String KEY_SPLASH_PATH = "splashPath";

    // map_preferences.xml
    public static final String KEY_BASEMAP_SOURCE = "basemap_source";

    // basemap styles
    public static final String KEY_GOOGLE_MAP_STYLE = "google_map_style";
    public static final String KEY_MAPBOX_MAP_STYLE = "mapbox_map_style";
    public static final String KEY_USGS_MAP_STYLE = "usgs_map_style";
    public static final String KEY_CARTO_MAP_STYLE = "carto_map_style";

    public static final String KEY_REFERENCE_LAYER = "reference_layer";

    // form_management_preferences.xml
    public static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    public static final String KEY_AUTOMATIC_UPDATE = "automatic_update";
    public static final String KEY_HIDE_OLD_FORM_VERSIONS = "hide_old_form_versions";
    public static final String KEY_AUTOSEND = "autosend";
    public static final String KEY_DELETE_AFTER_SEND = "delete_send";
    public static final String KEY_COMPLETED_DEFAULT = "default_completed";
    public static final String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";
    public static final String KEY_HIGH_RESOLUTION = "high_resolution";
    public static final String KEY_IMAGE_SIZE = "image_size";
    public static final String KEY_GUIDANCE_HINT = "guidance_hint";
    public static final String KEY_INSTANCE_SYNC = "instance_sync";
    public static final String KEY_FORM_UPDATE_MODE = "form_update_mode";

    // identity_preferences.xml
    public static final String KEY_ANALYTICS = "analytics";

    // form_metadata_preferences.xml
    public static final String KEY_METADATA_USERNAME = "metadata_username";
    public static final String KEY_METADATA_PHONENUMBER = "metadata_phonenumber";
    public static final String KEY_METADATA_EMAIL = "metadata_email";

    static final String KEY_FORM_METADATA = "form_metadata";

    public static final String KEY_BACKGROUND_LOCATION = "background_location";
    public static final String KEY_BACKGROUND_RECORDING = "background_recording";

    // values
    public static final String NAVIGATION_SWIPE = "swipe";
    public static final String NAVIGATION_BUTTONS = "buttons";
    public static final String NAVIGATION_BOTH = "swipe_buttons";
    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    private static final String AUTOSEND_OFF = "off";
    private static final String GUIDANCE_HINT_OFF = "no";
    static final String KEY_AUTOSEND_WIFI = "autosend_wifi";
    static final String KEY_AUTOSEND_NETWORK = "autosend_network";

    // basemap section
    public static final String CATEGORY_BASEMAP = "category_basemap";

    // basemap source values
    public static final String BASEMAP_SOURCE_GOOGLE = "google";
    public static final String BASEMAP_SOURCE_MAPBOX = "mapbox";
    public static final String BASEMAP_SOURCE_OSM = "osm";
    public static final String BASEMAP_SOURCE_USGS = "usgs";
    public static final String BASEMAP_SOURCE_STAMEN = "stamen";
    public static final String BASEMAP_SOURCE_CARTO = "carto";

    // start smap
    public static final String KEY_SMAP_REVIEW_FINAL = "review_final";    // Allow review of Form after finalising
    public static final String KEY_SMAP_USER_LOCATION = "smap_gps_trail";    // Record a user trail
    public static final String KEY_SMAP_USER_SAVE_LOCATION = "smap_gps_trail";    // Backup of decision to record the user trail
    public static final String KEY_SMAP_LOCATION_TRIGGER = "location_trigger";  // Enable triggering of forms by location
    public static final String KEY_SMAP_ODK_STYLE_MENUS = "odk_style_menus";  // Show ODK style menus as well as refresh
    public static final String KEY_SMAP_ODK_INSTANCENAME = "odk_instancename";  // Allow user to change instance name
    public static final String KEY_SMAP_ODK_MARK_FINALIZED = "odk_mark_finalized";  // Allow user to change instance name
    public static final String KEY_SMAP_PREVENT_DISABLE_TRACK = "disable_prevent_track";  // Prevent the user from disabling tracking
    public static final String KEY_SMAP_ENABLE_GEOFENCE = "enable_geofence";  // Monitor location for geofence
    public static final String KEY_SMAP_ODK_ADMIN_MENU = "odk_admin_menu";  // Show ODK admin menu
    public static final String KEY_SMAP_ADMIN_SERVER_MENU = "admin_server_menu";  // Show server menu in general settings
    public static final String KEY_SMAP_ADMIN_META_MENU = "admin_meta_menu";  // Show meta menu in general settings
    public static final String KEY_SMAP_EXIT_TRACK_MENU = "smap_exit_track_menu";  // Disable the exit track menu
    public static final String KEY_SMAP_BG_STOP_MENU = "smap_bg_stop_menu";  // Disable the exit track menu
    public static final String KEY_SMAP_OVERRIDE_SYNC = "smap_override_sync";  // Override the local settings for synchronisation
    public static final String KEY_SMAP_OVERRIDE_LOCATION = "smap_override_location";  // Override the local settings for user trail
    public static final String KEY_SMAP_OVERRIDE_DELETE = "smap_override_del";  // Override the local settings for delete after send
    public static final String KEY_SMAP_OVERRIDE_HIGH_RES_VIDEO = "smap_override_high_res_video";  // Override the local settings for video resolution
    public static final String KEY_SMAP_OVERRIDE_GUIDANCE = "smap_override_guidance";  // Override the local settings for guidance hint
    public static final String KEY_SMAP_OVERRIDE_IMAGE_SIZE = "smap_override_image_size";  // Override the local settings for the image size
    public static final String KEY_SMAP_OVERRIDE_NAVIGATION = "smap_override_navigation";  // Override the local settings for the screen navigation
    public static final String KEY_SMAP_REGISTRATION_ID = "registration_id";  // Android notifications id
    public static final String KEY_SMAP_REGISTRATION_SERVER = "registration_server";  // Server name that has been registered
    public static final String KEY_SMAP_REGISTRATION_USER = "registration_user";  // User name that has been registered
    public static final String KEY_SMAP_LAST_LOGIN = "last_login";  // System time in milli seconds that the user last logged in
    public static final String KEY_SMAP_PASSWORD_POLICY = "pw_policy";
    public static final String KEY_SMAP_CURRENT_ORGANISATION = "smap_current_organisation";
    public static final String KEY_SMAP_ORGANISATIONS = "smap_organisations";

    // GeoPoly recording settings
    public static final String KEY_SMAP_INPUT_METHOD = "smap_input_method";
    public static final String KEY_SMAP_IM_RI = "smap_im_ri";
    public static final String KEY_SMAP_IM_ACC = "smap_im_acc";
    // end smap

    // experimental
    public static final String KEY_MAGENTA_THEME = "magenta";
    public static final String KEY_EXTERNAL_APP_RECORDING = "external_app_recording";

    private static HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        // odk_server_preferences.xmll
        hashMap.put(KEY_SERVER_URL, Collect.getInstance().getString(R.string.default_server_url));
        hashMap.put(KEY_USERNAME, "");
        hashMap.put(KEY_PASSWORD, "");
        // form_management_preferences.xml
        hashMap.put(KEY_AUTOSEND, AUTOSEND_OFF);
        hashMap.put(KEY_GUIDANCE_HINT, GUIDANCE_HINT_OFF);
        hashMap.put(KEY_DELETE_AFTER_SEND, false);
        hashMap.put(KEY_COMPLETED_DEFAULT, true);
        hashMap.put(KEY_CONSTRAINT_BEHAVIOR, CONSTRAINT_BEHAVIOR_ON_SWIPE);
        hashMap.put(KEY_HIGH_RESOLUTION, true);
        hashMap.put(KEY_IMAGE_SIZE, "original_image_size");
        hashMap.put(KEY_INSTANCE_SYNC, true);
        hashMap.put(KEY_PERIODIC_FORM_UPDATES_CHECK, "every_fifteen_minutes");
        hashMap.put(KEY_AUTOMATIC_UPDATE, false);
        hashMap.put(KEY_HIDE_OLD_FORM_VERSIONS, true);
        hashMap.put(KEY_BACKGROUND_LOCATION, true);
        hashMap.put(KEY_BACKGROUND_RECORDING, true);
        hashMap.put(KEY_FORM_UPDATE_MODE, "manual");
        // form_metadata_preferences.xml
        hashMap.put(KEY_METADATA_USERNAME, "");
        hashMap.put(KEY_METADATA_PHONENUMBER, "");
        hashMap.put(KEY_METADATA_EMAIL, "");
        // google_preferences.xml
        hashMap.put(KEY_SELECTED_GOOGLE_ACCOUNT, "");
        hashMap.put(KEY_GOOGLE_SHEETS_URL, "");
        // identity_preferences.xml
        hashMap.put(KEY_ANALYTICS, true);
        // custom_server_paths_preferenceshs_preferences.xml
        hashMap.put(KEY_FORMLIST_URL, Collect.getInstance().getString(R.string.default_odk_formlist));
        hashMap.put(KEY_SUBMISSION_URL, Collect.getInstance().getString(R.string.default_odk_submission));
        // server_preferences.xml
        hashMap.put(KEY_PROTOCOL, Collect.getInstance().getString(R.string.protocol_odk_default));
        // user_interface_preferences.xml
        hashMap.put(KEY_APP_THEME, Collect.getInstance().getString(R.string.app_theme_light));
        hashMap.put(KEY_APP_LANGUAGE, "");
        hashMap.put(KEY_FONT_SIZE, String.valueOf(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
        hashMap.put(KEY_NAVIGATION, NAVIGATION_BOTH);
        hashMap.put(KEY_SHOW_SPLASH, false);
        hashMap.put(KEY_SPLASH_PATH, Collect.getInstance().getString(R.string.default_splash_path));
        hashMap.put(KEY_MAGENTA_THEME, false);
        hashMap.put(KEY_EXTERNAL_APP_RECORDING, true);

        // start smap
        hashMap.put(KEY_SMAP_REVIEW_FINAL, true);
        hashMap.put(KEY_SMAP_USER_LOCATION, false);
        hashMap.put(KEY_SMAP_LOCATION_TRIGGER, true);
        hashMap.put(KEY_SMAP_ODK_STYLE_MENUS, true);
        hashMap.put(KEY_SMAP_ODK_INSTANCENAME, false);
        hashMap.put(KEY_SMAP_ODK_MARK_FINALIZED, false);
        hashMap.put(KEY_SMAP_PREVENT_DISABLE_TRACK, false);
        hashMap.put(KEY_SMAP_ENABLE_GEOFENCE, true);    // Default geofence on until notififid otherwise
        hashMap.put(KEY_SMAP_ODK_ADMIN_MENU, false);
        hashMap.put(KEY_SMAP_ADMIN_SERVER_MENU, true);
        hashMap.put(KEY_SMAP_ADMIN_META_MENU, true);
        hashMap.put(KEY_SMAP_EXIT_TRACK_MENU, false);
        hashMap.put(KEY_SMAP_BG_STOP_MENU, false);

        hashMap.put(KEY_SMAP_OVERRIDE_SYNC, false);
        hashMap.put(KEY_SMAP_OVERRIDE_DELETE, false);
        hashMap.put(KEY_SMAP_OVERRIDE_HIGH_RES_VIDEO, false);
        hashMap.put(KEY_SMAP_OVERRIDE_GUIDANCE, false);
        hashMap.put(KEY_SMAP_OVERRIDE_IMAGE_SIZE, false);
        hashMap.put(KEY_SMAP_OVERRIDE_NAVIGATION, false);
        hashMap.put(KEY_SMAP_OVERRIDE_LOCATION, false);
        hashMap.put(KEY_SMAP_REGISTRATION_ID, "");
        hashMap.put(KEY_SMAP_REGISTRATION_SERVER, "");
        hashMap.put(KEY_SMAP_REGISTRATION_USER, "");
        hashMap.put(KEY_SMAP_LAST_LOGIN, "0");
        hashMap.put(KEY_SMAP_PASSWORD_POLICY, "-1");

        hashMap.put(KEY_SMAP_INPUT_METHOD, "not set");
        hashMap.put(KEY_SMAP_IM_RI, GeoPolyActivity.DEFAULT_INTERVAL_INDEX);
        hashMap.put(KEY_SMAP_IM_ACC, GeoPolyActivity.DEFAULT_ACCURACY_THRESHOLD_INDEX);
        // end smap

        // map_preferences.xml
        hashMap.put(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_GOOGLE);
        hashMap.put(KEY_CARTO_MAP_STYLE, "positron");
        hashMap.put(KEY_USGS_MAP_STYLE, "topographic");
        hashMap.put(KEY_GOOGLE_MAP_STYLE, String.valueOf(GoogleMap.MAP_TYPE_NORMAL));
        hashMap.put(KEY_MAPBOX_MAP_STYLE, Style.MAPBOX_STREETS);
        return hashMap;
    }

    public static final HashMap<String, Object> DEFAULTS = getHashMap();

    private GeneralKeys() {

    }

}
