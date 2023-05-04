package org.odk.collect.settings;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.settings.support.SettingsUtils.assertSettings;
import static org.odk.collect.settings.support.SettingsUtils.assertSettingsEmpty;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;
import static java.util.Arrays.asList;

import org.junit.Test;
import org.odk.collect.settings.keys.ProtectedProjectKeys;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

import java.util.List;

public class ODKAppSettingsMigratorTest {

    private final Settings unprotectedSettings = new InMemSettings();
    private final Settings protectedSettings = new InMemSettings();
    private final Settings metaSettings = new InMemSettings();

    @Test
    public void shouldMigrateGoogleMapSettings() {
        initSettings(unprotectedSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "google", "google_map_style", String.valueOf(1));

        initSettings(unprotectedSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "google", "google_map_style", String.valueOf(2));

        initSettings(unprotectedSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "google", "google_map_style", String.valueOf(3));

        initSettings(unprotectedSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "google", "google_map_style", String.valueOf(4));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/streets-v11");

        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/light-v10");

        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/dark-v10");

        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/satellite-v9");

        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/satellite-streets-v11");

        initSettings(unprotectedSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "mapbox", "mapbox_map_style", "mapbox://styles/mapbox/outdoors-v11");
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "osm");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "usgs", "usgs_map_style", "topographic");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "usgs", "usgs_map_style", "hybrid");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "usgs", "usgs_map_style", "satellite");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "stamen");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_positron");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "carto", "carto_map_style", "positron");

        initSettings(unprotectedSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_darkmatter");
        runMigrations();
        assertSettings(unprotectedSettings, "basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        initSettings(protectedSettings, "unrelated", "value");
        runMigrations();
        assertSettings(protectedSettings, "unrelated", "value");

        initSettings(protectedSettings, "show_map_sdk", true);
        runMigrations();
        assertSettings(protectedSettings, "show_map_sdk", true);

        initSettings(protectedSettings, "show_map_sdk", false);
        runMigrations();
        assertSettings(protectedSettings, "maps", false);

        initSettings(protectedSettings, "show_map_basemap", true);
        runMigrations();
        assertSettings(protectedSettings, "show_map_basemap", true);

        initSettings(protectedSettings, "show_map_basemap", false);
        runMigrations();
        assertSettings(protectedSettings, "maps", false);
    }

    @Test
    public void migratesMetaKeysToMetaPrefs() {
        initSettings(unprotectedSettings,
                "firstRun", true,
                "lastVersion", 1L,
                "scoped_storage_used", true,
                "metadata_migrated", true,
                "mapbox_initialized", true
        );

        runMigrations();

        assertSettingsEmpty(unprotectedSettings);
        assertSettings(metaSettings,
                "scoped_storage_used", true,
                "mapbox_initialized", true
        );
    }

    @Test
    public void migratesServerType() {
        initSettings(unprotectedSettings, "protocol", "other_protocol");
        runMigrations();
        assertSettings(unprotectedSettings, "protocol", "odk_default");
    }

    @Test
    public void migratesAutosendSettings() {
        initSettings(unprotectedSettings,
                "autosend_wifi", false,
                "autosend_network", false
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "autosend", "off"
        );

        initSettings(unprotectedSettings,
                "autosend_wifi", true,
                "autosend_network", false
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "autosend", "wifi_only"
        );

        initSettings(unprotectedSettings,
                "autosend_wifi", false,
                "autosend_network", true
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "autosend", "cellular_only"
        );

        initSettings(unprotectedSettings,
                "autosend_wifi", true,
                "autosend_network", true
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "autosend", "wifi_and_cellular"
        );
    }

    @Test
    public void migratesFormUpdateModeSettings() {
        initSettings(unprotectedSettings,
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );

        List<String> periods = asList("every_fifteen_minutes", "every_one_hour", "every_six_hours", "every_24_hours");
        for (String period : periods) {
            initSettings(unprotectedSettings,
                    "periodic_form_updates_check", period
            );
            runMigrations();
            assertSettings(unprotectedSettings,
                    "periodic_form_updates_check", period,
                    "form_update_mode", "previously_downloaded"
            );
        }

        initSettings(unprotectedSettings,
                "protocol", "google_sheets"
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual"
        );

        initSettings(unprotectedSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "every_24_hours"
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_24_hours"
        );

        initSettings(unprotectedSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        assertSettings(unprotectedSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );
    }

    @Test
    public void migratesServerList() {
        initSettings(unprotectedSettings,
                "knownUrlList", "[\"http://blah.com\"]"
        );

        runMigrations();
        assertSettingsEmpty(unprotectedSettings);
        assertSettings(metaSettings,
                "server_list", "[\"http://blah.com\"]"
        );
    }

    @Test
    public void when_markAsFinalized_wasDisabled_and_defaultCompleted_wasDisabled_thenDisableNew_finalize_andRemoveOldSettings() {
        initSettings(protectedSettings, "mark_as_finalized", false);
        initSettings(unprotectedSettings, "default_completed", false);

        runMigrations();

        assertSettings(protectedSettings, ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, true, ProtectedProjectKeys.KEY_FINALIZE, false);

        assertThat(protectedSettings.contains("mark_as_finalized"), equalTo(false));
        assertThat(protectedSettings.contains("default_completed"), equalTo(false));
    }

    @Test
    public void when_markAsFinalized_wasDisabled_and_defaultCompleted_wasEnabled_thenDisableNew_saveAsDraft_andRemoveOldSettings() {
        initSettings(protectedSettings, "mark_as_finalized", false);
        initSettings(unprotectedSettings, "default_completed", true);

        runMigrations();

        assertSettings(protectedSettings, ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false, ProtectedProjectKeys.KEY_FINALIZE, true);

        assertThat(protectedSettings.contains("mark_as_finalized"), equalTo(false));
        assertThat(protectedSettings.contains("default_completed"), equalTo(false));
    }

    @Test
    public void when_markAsFinalized_wasDisabled_and_defaultCompleted_wasNotSet_thenDisableNew_finalize_andRemoveOldSettings() {
        initSettings(protectedSettings, "mark_as_finalized", false);

        runMigrations();

        assertSettings(protectedSettings, ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false, ProtectedProjectKeys.KEY_FINALIZE, true);

        assertThat(protectedSettings.contains("mark_as_finalized"), equalTo(false));
        assertThat(protectedSettings.contains("default_completed"), equalTo(false));
    }

    @Test
    public void when_markAsFinalized_wasEnabled_and_defaultCompleted_wasDisabled_thenDoNotUpdateSettingsAndRemoveOldOnes() {
        initSettings(protectedSettings, "mark_as_finalized", true);
        initSettings(unprotectedSettings, "default_completed", false);

        runMigrations();

        assertSettingsEmpty(protectedSettings);

        assertThat(protectedSettings.contains("mark_as_finalized"), equalTo(false));
        assertThat(protectedSettings.contains("default_completed"), equalTo(false));
    }

    @Test
    public void when_markAsFinalized_wasEnabled_and_defaultCompleted_wasEnabled_thenDoNotUpdateSettingsAndRemoveOldOnes() {
        initSettings(protectedSettings, "mark_as_finalized", true);
        initSettings(unprotectedSettings, "default_completed", true);

        runMigrations();

        assertSettingsEmpty(protectedSettings);

        assertThat(protectedSettings.contains("mark_as_finalized"), equalTo(false));
        assertThat(protectedSettings.contains("default_completed"), equalTo(false));
    }

    private void runMigrations() {
        new ODKAppSettingsMigrator(metaSettings).migrate(unprotectedSettings, protectedSettings);
    }
}
