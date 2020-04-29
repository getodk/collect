package org.odk.collect.android.application.initialization;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PrefMigratorTest {

    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
    }

    private void initPrefs(Object... pairs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (pairs[i + 1] instanceof String) {
                editor.putString((String) pairs[i], (String) pairs[i + 1]);
            } else if (pairs[i + 1] instanceof Boolean) {
                editor.putBoolean((String) pairs[i], (Boolean) pairs[i + 1]);
            }
        }
        editor.commit();
    }

    private void assertPrefs(Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            assertEquals(pairs[i + 1], prefs.getAll().get(pairs[i]));
        }
        assertEquals(pairs.length / 2, prefs.getAll().size());
    }

    @Test
    public void shouldMigrateGoogleMapSettings() {
        initPrefs("map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_NORMAL));

        initPrefs("map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_SATELLITE));

        initPrefs("map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_TERRAIN));

        initPrefs("map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_HYBRID));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.MAPBOX_STREETS);

        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.LIGHT);

        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.DARK);

        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE);

        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE_STREETS);

        initPrefs("map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "mapbox", "mapbox_map_style", Style.OUTDOORS);
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "osm");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "usgs", "usgs_map_style", "topographic");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "usgs", "usgs_map_style", "hybrid");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "usgs", "usgs_map_style", "satellite");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "stamen");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_carto_positron");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "carto", "carto_map_style", "positron");

        initPrefs("map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_carto_darkmatter");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        prefs = AdminSharedPreferences.getInstance().getSharedPreferences();

        initPrefs("unrelated", "value");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("unrelated", "value");

        initPrefs("show_map_sdk", true);
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("show_map_sdk", true);

        initPrefs("show_map_sdk", false);
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("maps", false);

        initPrefs("show_map_basemap", true);
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("show_map_basemap", true);

        initPrefs("show_map_basemap", false);
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("maps", false);
    }

    @Test
    public void migratesServerType() {
        initPrefs("protocol", "other_protocol");
        PrefMigrator.migrateSharedPrefs();
        assertPrefs("protocol", "odk_default");
    }
}
