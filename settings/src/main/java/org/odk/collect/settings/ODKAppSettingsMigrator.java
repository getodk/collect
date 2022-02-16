package org.odk.collect.settings;

import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_STAMEN;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_USGS_MAP_STYLE;
import static org.odk.collect.settings.migration.MigrationUtils.combineKeys;
import static org.odk.collect.settings.migration.MigrationUtils.extractNewKey;
import static org.odk.collect.settings.migration.MigrationUtils.moveKey;
import static org.odk.collect.settings.migration.MigrationUtils.removeKey;
import static org.odk.collect.settings.migration.MigrationUtils.renameKey;
import static org.odk.collect.settings.migration.MigrationUtils.translateKey;
import static org.odk.collect.settings.migration.MigrationUtils.translateValue;
import static java.util.Arrays.asList;

import org.odk.collect.settings.importing.SettingsMigrator;
import org.odk.collect.settings.migration.KeyRenamer;
import org.odk.collect.settings.migration.KeyTranslator;
import org.odk.collect.settings.migration.Migration;
import org.odk.collect.shared.settings.Settings;

import java.util.List;

/**
 * Migrates old preference keys and values to new ones.
 */
public class ODKAppSettingsMigrator implements SettingsMigrator {

    private final Settings metaPrefs;

    public ODKAppSettingsMigrator(Settings metaPrefs) {
        this.metaPrefs = metaPrefs;
    }

    @Override
    public void migrate(Settings generalSettings, Settings adminSettings) {
        for (Migration migration : getUnprotectedMigrations()) {
            migration.apply(generalSettings);
        }

        for (Migration migration : getProtectedMigrations()) {
            migration.apply(adminSettings);
        }

        for (Migration migration : getMetaMigrations()) {
            migration.apply(metaPrefs);
        }
    }

    private List<Migration> getUnprotectedMigrations() {
        return asList(
                translateKey("map_sdk_behavior").toKey(KEY_BASEMAP_SOURCE)
                        .fromValue("google_maps").toValue("google")
                        .fromValue("mapbox_maps").toValue("mapbox"),

                // ListPreferences can only handle string values, so we use string values here.
                // Note that unfortunately there was a hidden U+200E character in the preference
                // value for "terrain" in previous versions of ODK Collect, so we need to
                // include that character to match that value correctly.
                translateKey("map_basemap_behavior").toKey(KEY_GOOGLE_MAP_STYLE)
                        .fromValue("streets").toValue(Integer.toString(1))
                        .fromValue("terrain\u200e").toValue(Integer.toString(3))
                        .fromValue("terrain").toValue(Integer.toString(3))
                        .fromValue("hybrid").toValue(Integer.toString(4))
                        .fromValue("satellite").toValue(Integer.toString(2)),

                translateKey("map_basemap_behavior").toKey(KEY_MAPBOX_MAP_STYLE)
                        .fromValue("mapbox_streets").toValue("mapbox://styles/mapbox/streets-v11")
                        .fromValue("mapbox_light").toValue("mapbox://styles/mapbox/light-v10")
                        .fromValue("mapbox_dark").toValue("mapbox://styles/mapbox/dark-v10")
                        .fromValue("mapbox_satellite").toValue("mapbox://styles/mapbox/satellite-v9")
                        .fromValue("mapbox_satellite_streets").toValue("mapbox://styles/mapbox/satellite-streets-v11")
                        .fromValue("mapbox_outdoors").toValue("mapbox://styles/mapbox/outdoors-v11"),

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

                        .withValues("osmdroid", "openmap_cartodb_positron")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_CARTO, KEY_CARTO_MAP_STYLE, "positron")
                        .withValues("osmdroid", "openmap_cartodb_darkmatter")
                        .toPairs(KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_CARTO, KEY_CARTO_MAP_STYLE, "dark_matter"),

                translateValue("other_protocol").toValue("odk_default").forKey("protocol"),

                removeKey("firstRun"),
                removeKey("lastVersion"),
                moveKey("scoped_storage_used").toPreferences(metaPrefs),
                removeKey("metadata_migrated"),
                moveKey("mapbox_initialized").toPreferences(metaPrefs),

                combineKeys("autosend_wifi", "autosend_network")
                        .withValues(false, false).toPairs("autosend", "off")
                        .withValues(false, true).toPairs("autosend", "cellular_only")
                        .withValues(true, false).toPairs("autosend", "wifi_only")
                        .withValues(true, true).toPairs("autosend", "wifi_and_cellular"),

                extractNewKey("form_update_mode").fromKey("protocol")
                        .fromValue("google_sheets").toValue("manual"),

                extractNewKey("form_update_mode").fromKey("periodic_form_updates_check")
                        .fromValue("never").toValue("manual")
                        .fromValue("every_fifteen_minutes").toValue("previously_downloaded")
                        .fromValue("every_one_hour").toValue("previously_downloaded")
                        .fromValue("every_six_hours").toValue("previously_downloaded")
                        .fromValue("every_24_hours").toValue("previously_downloaded"),

                translateValue("never").toValue("every_fifteen_minutes").forKey("periodic_form_updates_check"),

                moveKey("knownUrlList").toPreferences(metaPrefs)
        );
    }

    public List<KeyRenamer> getMetaMigrations() {
        return asList(
                renameKey("firstRun").toKey("first_run"),
                renameKey("lastVersion").toKey("last_version"),

                renameKey("knownUrlList").toKey("server_list")
        );
    }

    public List<KeyTranslator> getProtectedMigrations() {
        return asList(
                // When either the map SDK or the basemap selection were previously
                // hidden, we want to hide the entire Maps preference screen.
                translateKey("show_map_sdk").toKey("maps")
                        .fromValue(false).toValue(false),
                translateKey("show_map_basemap").toKey("maps")
                        .fromValue(false).toValue(false)
        );
    }
}
