package org.odk.collect.settings;

import static org.odk.collect.settings.support.SettingsUtils.assertSettings;
import static org.odk.collect.settings.support.SettingsUtils.assertSettingsEmpty;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;
import static java.util.Arrays.asList;

import org.junit.Test;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

import java.util.List;

public class ODKAppSettingsMigratorTest {

    private final Settings generalSettings = new InMemSettings();
    private final Settings adminSettings = new InMemSettings();
    private final Settings metaSettings = new InMemSettings();

    @Test
    public void shouldMigrateGoogleMapSettings() {
        initSettings(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(1));

        initSettings(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(2));

        initSettings(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(3));

        initSettings(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(4));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/streets-v11");

        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/light-v10");

        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/dark-v10");

        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/satellite-v9");

        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/satellite-streets-v11");

        initSettings(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/outdoors-v11");
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "osm");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "usgs", "usgs_map_style", "topographic");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "usgs", "usgs_map_style", "hybrid");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "usgs", "usgs_map_style", "satellite");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "stamen");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_positron");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "carto", "carto_map_style", "positron");

        initSettings(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_darkmatter");
        runMigrations();
        assertSettings(generalSettings, "basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        initSettings(adminSettings, "unrelated", "value");
        runMigrations();
        assertSettings(adminSettings, "unrelated", "value");

        initSettings(adminSettings, "show_map_sdk", true);
        runMigrations();
        assertSettings(adminSettings, "show_map_sdk", true);

        initSettings(adminSettings, "show_map_sdk", false);
        runMigrations();
        assertSettings(adminSettings, "maps", false);

        initSettings(adminSettings, "show_map_basemap", true);
        runMigrations();
        assertSettings(adminSettings, "show_map_basemap", true);

        initSettings(adminSettings, "show_map_basemap", false);
        runMigrations();
        assertSettings(adminSettings, "maps", false);
    }

    @Test
    public void migratesMetaKeysToMetaPrefs() {
        initSettings(generalSettings,
                "firstRun", true,
                "lastVersion", 1L,
                "scoped_storage_used", true,
                "metadata_migrated", true,
                "mapbox_initialized", true
        );

        runMigrations();

        assertSettingsEmpty(generalSettings);
        assertSettings(metaSettings,
                "scoped_storage_used", true,
                "mapbox_initialized", true
        );
    }

    @Test
    public void migratesServerType() {
        initSettings(generalSettings, "protocol", "other_protocol");
        runMigrations();
        assertSettings(generalSettings, "protocol", "odk_default");
    }

    @Test
    public void migratesAutosendSettings() {
        initSettings(generalSettings,
                "autosend_wifi", false,
                "autosend_network", false
        );
        runMigrations();
        assertSettings(generalSettings,
                "autosend", "off"
        );

        initSettings(generalSettings,
                "autosend_wifi", true,
                "autosend_network", false
        );
        runMigrations();
        assertSettings(generalSettings,
                "autosend", "wifi_only"
        );

        initSettings(generalSettings,
                "autosend_wifi", false,
                "autosend_network", true
        );
        runMigrations();
        assertSettings(generalSettings,
                "autosend", "cellular_only"
        );

        initSettings(generalSettings,
                "autosend_wifi", true,
                "autosend_network", true
        );
        runMigrations();
        assertSettings(generalSettings,
                "autosend", "wifi_and_cellular"
        );
    }

    @Test
    public void migratesFormUpdateModeSettings() {
        initSettings(generalSettings,
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertSettings(generalSettings,
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );

        List<String> periods = asList("every_fifteen_minutes", "every_one_hour", "every_six_hours", "every_24_hours");
        for (String period : periods) {
            initSettings(generalSettings,
                    "periodic_form_updates_check", period
            );
            runMigrations();
            assertSettings(generalSettings,
                    "periodic_form_updates_check", period,
                    "form_update_mode", "previously_downloaded"
            );
        }

        initSettings(generalSettings,
                "protocol", "google_sheets"
        );
        runMigrations();
        assertSettings(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual"
        );

        initSettings(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "every_24_hours"
        );
        runMigrations();
        assertSettings(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_24_hours"
        );

        initSettings(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertSettings(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );
    }

    @Test
    public void migratesServerList() {
        initSettings(generalSettings,
                "knownUrlList", "[\"http://blah.com\"]"
        );

        runMigrations();
        assertSettingsEmpty(generalSettings);
        assertSettings(metaSettings,
                "server_list", "[\"http://blah.com\"]"
        );
    }

    private void runMigrations() {
        new ODKAppSettingsMigrator(metaSettings).migrate(generalSettings, adminSettings);
    }
}
