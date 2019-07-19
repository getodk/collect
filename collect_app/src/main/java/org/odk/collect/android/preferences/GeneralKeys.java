package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public final class GeneralKeys {

    // aggregate_preferences.xml
    public static final String KEY_SERVER_URL               = "server_url";
    public static final String KEY_USERNAME                 = "username";
    public static final String KEY_PASSWORD                 = "password";

    // form_management_preferences.xml
    public static final String KEY_AUTOSEND                 = "autosend";
    public static final String KEY_DELETE_AFTER_SEND        = "delete_send";
    public static final String KEY_COMPLETED_DEFAULT        = "default_completed";
    public static final String KEY_CONSTRAINT_BEHAVIOR      = "constraint_behavior";
    public static final String KEY_HIGH_RESOLUTION          = "high_resolution";
    public static final String KEY_IMAGE_SIZE               = "image_size";
    public static final String KEY_GUIDANCE_HINT            = "guidance_hint";
    public static final String KEY_INSTANCE_SYNC            = "instance_sync";
    public static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    public static final String KEY_AUTOMATIC_UPDATE         = "automatic_update";
    public static final String KEY_HIDE_OLD_FORM_VERSIONS   = "hide_old_form_versions";
    public static final String KEY_BACKGROUND_LOCATION      = "background_location";

    // form_metadata_preferences.xml
    public static final String KEY_METADATA_USERNAME        = "metadata_username";
    public static final String KEY_METADATA_PHONENUMBER     = "metadata_phonenumber";
    public static final String KEY_METADATA_EMAIL           = "metadata_email";

    // google_preferences.xml
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT  = "selected_google_account";
    public static final String KEY_GOOGLE_SHEETS_URL        = "google_sheets_url";

    // identity_preferences.xml
    static final String KEY_FORM_METADATA                   = "form_metadata";
    public static final String KEY_ANALYTICS                = "analytics";

    // other_preferences.xml
    public static final String KEY_FORMLIST_URL             = "formlist_url";
    public static final String KEY_SUBMISSION_URL           = "submission_url";

    // server_preferences.xml
    public static final String KEY_PROTOCOL                 = "protocol";
    public static final String KEY_SMS_GATEWAY              = "sms_gateway";
    public static final String KEY_SUBMISSION_TRANSPORT_TYPE = "submission_transport_type";
    public static final String KEY_TRANSPORT_PREFERENCE      = "submission_transport_preference";
    public static final String KEY_SMS_PREFERENCE            = "sms_preference";

    // user_interface_preferences.xml
    public static final String KEY_APP_THEME                = "appTheme";
    public static final String KEY_APP_LANGUAGE             = "app_language";
    public static final String KEY_FONT_SIZE                = "font_size";
    public static final String KEY_NAVIGATION               = "navigation";
    public static final String KEY_SHOW_SPLASH              = "showSplash";
    public static final String KEY_SPLASH_PATH              = "splashPath";

    // map_preferences.xml
    public static final String CATEGORY_BASE_LAYER          = "category_base_layer";
    public static final String KEY_BASE_LAYER_SOURCE        = "base_layer_source";
    public static final String CATEGORY_REFERENCE_LAYER     = "category_reference_layer";
    public static final String KEY_REFERENCE_LAYER          = "reference_layer";
    public static final String BASE_LAYER_SOURCE_GOOGLE     = "base_layer_source_google";
    public static final String BASE_LAYER_SOURCE_MAPBOX     = "base_layer_source_mapbox";
    public static final String BASE_LAYER_SOURCE_OSM        = "base_layer_source_osm";
    public static final String BASE_LAYER_SOURCE_USGS       = "base_layer_source_usgs";
    public static final String BASE_LAYER_SOURCE_STAMEN     = "base_layer_source_stamen";
    public static final String BASE_LAYER_SOURCE_CARTO      = "base_layer_source_carto";

    public static final String KEY_GOOGLE_MAP_STYLE         = "google_map_style";
    public static final String KEY_MAPBOX_MAP_STYLE         = "mapbox_map_style";
    public static final String KEY_USGS_MAP_STYLE           = "usgs_map_style";
    public static final String KEY_CARTO_MAP_STYLE          = "carto_map_style";

    // other keys
    public static final String KEY_LAST_VERSION             = "lastVersion";
    public static final String KEY_FIRST_RUN                = "firstRun";
    /** Whether any existing username and email values have been migrated to form metadata */
    static final String KEY_METADATA_MIGRATED               = "metadata_migrated";
    static final String KEY_AUTOSEND_WIFI                   = "autosend_wifi";
    static final String KEY_AUTOSEND_NETWORK                = "autosend_network";

    // values
    public static final String NAVIGATION_SWIPE             = "swipe";
    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    public static final String NAVIGATION_BUTTONS           = "buttons";
    private static final String AUTOSEND_OFF                = "off";
    private static final String GUIDANCE_HINT_OFF           = "no";

    // These values match those in map_sdk_selector_entry_values.
    public static final String GOOGLE_MAPS_BASEMAP_KEY      = "google_maps";
    public static final String OSM_BASEMAP_KEY              = "osmdroid";
    public static final String MAPBOX_BASEMAP_KEY           = "mapbox_maps";
    public static final String DEFAULT_BASEMAP_KEY = GOOGLE_MAPS_BASEMAP_KEY;

    public static final String GOOGLE_MAPS_BASEMAP_DEFAULT  = "streets";

    public static final String OSM_MAPS_BASEMAP_DEFAULT     = "openmap_streets";

    // These values match those in map_mapbox_basemap_selector_entry_values.
    public static final String MAPBOX_MAP_STREETS           = "mapbox_streets";
    public static final String MAPBOX_MAP_LIGHT             = "mapbox_light";
    public static final String MAPBOX_MAP_DARK              = "mapbox_dark";
    public static final String MAPBOX_MAP_SATELLITE         = "mapbox_satellite";
    public static final String MAPBOX_MAP_SATELLITE_STREETS = "mapbox_satellite_streets";
    public static final String MAPBOX_MAP_OUTDOORS          = "mapbox_outdoors";
    public static final String MAPBOX_BASEMAP_DEFAULT       = "mapbox_streets";

    private static HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        // aggregate_preferences.xml
        hashMap.put(KEY_SERVER_URL,                 Collect.getInstance().getString(R.string.default_server_url));
        hashMap.put(KEY_USERNAME,                   "");
        hashMap.put(KEY_PASSWORD,                   "");
        // form_management_preferences.xml
        hashMap.put(KEY_AUTOSEND,                   AUTOSEND_OFF);
        hashMap.put(KEY_GUIDANCE_HINT,              GUIDANCE_HINT_OFF);
        hashMap.put(KEY_DELETE_AFTER_SEND,          false);
        hashMap.put(KEY_COMPLETED_DEFAULT,          true);
        hashMap.put(KEY_CONSTRAINT_BEHAVIOR,        CONSTRAINT_BEHAVIOR_ON_SWIPE);
        hashMap.put(KEY_HIGH_RESOLUTION,            true);
        hashMap.put(KEY_IMAGE_SIZE,                 "original_image_size");
        hashMap.put(KEY_INSTANCE_SYNC,              true);
        hashMap.put(KEY_PERIODIC_FORM_UPDATES_CHECK, "never");
        hashMap.put(KEY_AUTOMATIC_UPDATE,           false);
        hashMap.put(KEY_HIDE_OLD_FORM_VERSIONS,     true);
        hashMap.put(KEY_BACKGROUND_LOCATION,        true);
        // form_metadata_preferences.xml
        hashMap.put(KEY_METADATA_USERNAME,          "");
        hashMap.put(KEY_METADATA_PHONENUMBER,       "");
        hashMap.put(KEY_METADATA_EMAIL,             "");
        // google_preferences.xml
        hashMap.put(KEY_SELECTED_GOOGLE_ACCOUNT,    "");
        hashMap.put(KEY_GOOGLE_SHEETS_URL,          "");
        // identity_preferences.xml
        hashMap.put(KEY_ANALYTICS,                  true);
        // other_preferences.xml
        hashMap.put(KEY_FORMLIST_URL,               Collect.getInstance().getString(R.string.default_odk_formlist));
        hashMap.put(KEY_SUBMISSION_URL,             Collect.getInstance().getString(R.string.default_odk_submission));
        // server_preferences.xml
        hashMap.put(KEY_PROTOCOL,                   Collect.getInstance().getString(R.string.protocol_odk_default));
        hashMap.put(KEY_SMS_GATEWAY,                "");
        hashMap.put(KEY_SUBMISSION_TRANSPORT_TYPE,  Collect.getInstance().getString(R.string.transport_type_value_internet));
        // user_interface_preferences.xml
        hashMap.put(KEY_APP_THEME,                  Collect.getInstance().getString(R.string.app_theme_light));
        hashMap.put(KEY_APP_LANGUAGE,               "");
        hashMap.put(KEY_FONT_SIZE,                  Collect.DEFAULT_FONTSIZE);
        hashMap.put(KEY_NAVIGATION,                 NAVIGATION_SWIPE);
        hashMap.put(KEY_SHOW_SPLASH,                false);
        hashMap.put(KEY_SPLASH_PATH,                Collect.getInstance().getString(R.string.default_splash_path));
        // map_preferences.xml
        hashMap.put(KEY_BASE_LAYER_SOURCE, BASE_LAYER_SOURCE_GOOGLE);
        return hashMap;
    }

    static final Collection<String> KEYS_WE_SHOULD_NOT_RESET = Arrays.asList(
            KEY_LAST_VERSION,
            KEY_FIRST_RUN,
            KEY_METADATA_MIGRATED,
            KEY_AUTOSEND_WIFI,
            KEY_AUTOSEND_NETWORK
    );

    public static final HashMap<String, Object> GENERAL_KEYS = getHashMap();

    private GeneralKeys() {

    }

}
