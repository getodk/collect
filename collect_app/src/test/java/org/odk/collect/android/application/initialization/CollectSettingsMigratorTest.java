package org.odk.collect.android.application.initialization;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;

import java.util.List;

import static java.util.Arrays.asList;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
public class CollectSettingsMigratorTest {

    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();
    private final Settings metaSettings = TestSettingsProvider.getMetaSettings();

    @Before
    public void setUp() throws Exception {
        generalSettings.clear();
        adminSettings.clear();
        metaSettings.clear();
    }

    @Test
    public void shouldMigrateGoogleMapSettings() {
        initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_NORMAL));

        initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_SATELLITE));

        initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_TERRAIN));

        initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_HYBRID));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.MAPBOX_STREETS);

        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.LIGHT);

        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.DARK);

        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE);

        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE_STREETS);

        initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.OUTDOORS);
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "osm");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "topographic");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "hybrid");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "satellite");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "stamen");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_positron");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "carto", "carto_map_style", "positron");

        initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_darkmatter");
        runMigrations();
        assertPrefs(generalSettings, "basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        initPrefs(adminSettings, "unrelated", "value");
        runMigrations();
        assertPrefs(adminSettings, "unrelated", "value");

        initPrefs(adminSettings, "show_map_sdk", true);
        runMigrations();
        assertPrefs(adminSettings, "show_map_sdk", true);

        initPrefs(adminSettings, "show_map_sdk", false);
        runMigrations();
        assertPrefs(adminSettings, "maps", false);

        initPrefs(adminSettings, "show_map_basemap", true);
        runMigrations();
        assertPrefs(adminSettings, "show_map_basemap", true);

        initPrefs(adminSettings, "show_map_basemap", false);
        runMigrations();
        assertPrefs(adminSettings, "maps", false);
    }

    @Test
    public void migratesMetaKeysToMetaPrefs() {
        initPrefs(generalSettings,
                "firstRun", true,
                "lastVersion", 1L,
                "scoped_storage_used", true,
                "metadata_migrated", true,
                "mapbox_initialized", true
        );

        runMigrations();

        assertPrefsEmpty(generalSettings);
        assertPrefs(metaSettings,
                "scoped_storage_used", true,
                "mapbox_initialized", true
        );
    }

    @Test
    public void migratesServerType() {
        initPrefs(generalSettings, "protocol", "other_protocol");
        runMigrations();
        assertPrefs(generalSettings, "protocol", "odk_default");
    }

    @Test
    public void migratesAutosendSettings() {
        initPrefs(generalSettings,
                "autosend_wifi", false,
                "autosend_network", false
        );
        runMigrations();
        assertPrefs(generalSettings,
                "autosend", "off"
        );

        initPrefs(generalSettings,
                "autosend_wifi", true,
                "autosend_network", false
        );
        runMigrations();
        assertPrefs(generalSettings,
                "autosend", "wifi_only"
        );

        initPrefs(generalSettings,
                "autosend_wifi", false,
                "autosend_network", true
        );
        runMigrations();
        assertPrefs(generalSettings,
                "autosend", "cellular_only"
        );

        initPrefs(generalSettings,
                "autosend_wifi", true,
                "autosend_network", true
        );
        runMigrations();
        assertPrefs(generalSettings,
                "autosend", "wifi_and_cellular"
        );
    }

    @Test
    public void migratesFormUpdateModeSettings() {
        initPrefs(generalSettings,
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertPrefs(generalSettings,
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );

        List<String> periods = asList("every_fifteen_minutes", "every_one_hour", "every_six_hours", "every_24_hours");
        for (String period : periods) {
            initPrefs(generalSettings,
                    "periodic_form_updates_check", period
            );
            runMigrations();
            assertPrefs(generalSettings,
                    "periodic_form_updates_check", period,
                    "form_update_mode", "previously_downloaded"
            );
        }

        initPrefs(generalSettings,
                "protocol", "google_sheets"
        );
        runMigrations();
        assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual"
        );

        initPrefs(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "every_24_hours"
        );
        runMigrations();
        assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_24_hours"
        );

        initPrefs(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );
    }

    @Test
    public void migratesServerList() {
        initPrefs(generalSettings,
                "knownUrlList", "[\"http://blah.com\"]"
        );

        runMigrations();
        assertPrefsEmpty(generalSettings);
        assertPrefs(metaSettings,
                "server_list", "[\"http://blah.com\"]"
        );
    }

    private void runMigrations() {
        new CollectSettingsMigrator(metaSettings).migrate(generalSettings, adminSettings);
    }
}
