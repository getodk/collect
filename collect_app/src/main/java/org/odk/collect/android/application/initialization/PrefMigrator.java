package org.odk.collect.android.application.initialization;

import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.application.initialization.migration.Migration;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.combineKeys;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.moveKey;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.removeKey;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.renameKey;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateKey;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateValue;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_STAMEN;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USGS_MAP_STYLE;

/**
 * Migrates old preference keys and values to new ones.
 */
class PrefMigrator {

    private final SharedPreferences generalSharedPreferences;
    private final SharedPreferences adminSharedPreferences;
    private final SharedPreferences metaSharedPreferences;

    PrefMigrator(SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences, SharedPreferences metaSharedPreferences) {
        this.generalSharedPreferences = generalSharedPreferences;
        this.adminSharedPreferences = adminSharedPreferences;
        this.metaSharedPreferences = metaSharedPreferences;
    }

    void migrate() {
        migrate(generalSharedPreferences, getGeneralMigrations());
        migrate(metaSharedPreferences, getMetaMigrations());
        migrate(adminSharedPreferences, getAdminMigrations());
    }

    private static void migrate(SharedPreferences prefs, Migration... migrations) {
        for (Migration migration : migrations) {
            migration.apply(prefs);
        }
    }

    private Migration[] getGeneralMigrations() {
        return new Migration[]{
                translateKey("map_sdk_behavior").toKey(KEY_BASEMAP_SOURCE)
                        .fromValue("google_maps").toValue("google")
                        .fromValue("mapbox_maps").toValue("mapbox"),

                // ListPreferences can only handle string values, so we use string values here.
                // Note that unfortunately there was a hidden U+200E character in the preference
                // value for "terrain" in previous versions of ODK Collect, so we need to
                // include that character to match that value correctly.
                translateKey("map_basemap_behavior").toKey(KEY_GOOGLE_MAP_STYLE)
                        .fromValue("streets").toValue(Integer.toString(GoogleMap.MAP_TYPE_NORMAL))
                        .fromValue("terrain\u200e").toValue(Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))
                        .fromValue("terrain").toValue(Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))
                        .fromValue("hybrid").toValue(Integer.toString(GoogleMap.MAP_TYPE_HYBRID))
                        .fromValue("satellite").toValue(Integer.toString(GoogleMap.MAP_TYPE_SATELLITE)),

                translateKey("map_basemap_behavior").toKey(KEY_MAPBOX_MAP_STYLE)
                        .fromValue("mapbox_streets").toValue(Style.MAPBOX_STREETS)
                        .fromValue("mapbox_light").toValue(Style.LIGHT)
                        .fromValue("mapbox_dark").toValue(Style.DARK)
                        .fromValue("mapbox_satellite").toValue(Style.SATELLITE)
                        .fromValue("mapbox_satellite_streets").toValue(Style.SATELLITE_STREETS)
                        .fromValue("mapbox_outdoors").toValue(Style.OUTDOORS),

                // When the map_sdk_behavior is "osmdroid", we have to also examine the
                // map_basemap_behavior key to determine the new basemap source.
                combineKeys("map_sdk_behavior", "map_basemap_behavior")
                        .withValues("osmdroid", "openmap_streets")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_OSM)

                        .withValues("osmdroid", "openmap_usgs_topo")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_USGS, KEY_USGS_MAP_STYLE, "topographic")
                        .withValues("osmdroid", "openmap_usgs_sat")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_USGS, KEY_USGS_MAP_STYLE, "hybrid")
                        .withValues("osmdroid", "openmap_usgs_img")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_USGS, KEY_USGS_MAP_STYLE, "satellite")

                        .withValues("osmdroid", "openmap_stamen_terrain")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_STAMEN)

                        .withValues("osmdroid", "openmap_carto_positron")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_CARTO, KEY_CARTO_MAP_STYLE, "positron")
                        .withValues("osmdroid", "openmap_carto_darkmatter")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_CARTO, KEY_CARTO_MAP_STYLE, "dark_matter"),

                translateValue("other_protocol").toValue("odk_default").forKey("protocol"),

                removeKey("firstRun"),
                removeKey("lastVersion"),
                moveKey("scoped_storage_used").toPreferences(metaSharedPreferences),
                removeKey("metadata_migrated"),
                moveKey("mapbox_initialized").toPreferences(metaSharedPreferences)
        };
    }

    private Migration[] getMetaMigrations() {
        return new Migration[]{
                renameKey("firstRun").toKey("first_run"),
                renameKey("lastVersion").toKey("last_version")
        };
    }

    private Migration[] getAdminMigrations() {
        return new Migration[]{
                // When either the map SDK or the basemap selection were previously
                // hidden, we want to hide the entire Maps preference screen.
                translateKey("show_map_sdk").toKey("maps")
                        .fromValue(false).toValue(false),
                translateKey("show_map_basemap").toKey("maps")
                        .fromValue(false).toValue(false)
        };
    }
}
