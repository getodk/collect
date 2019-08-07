package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PrefMigrator.Migration;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PrefMigratorTest {

    private SharedPreferences prefs;

    // Renames a couple of keys.
    private static final Migration[] RENAMERS = {
        PrefMigrator.renameKey("colour").toKey("couleur"),
        PrefMigrator.renameKey("vegetable").toKey("legume")
    };

    // Renames a couple of keys, also translating their values.
    private static final Migration[] TRANSLATORS = {
        PrefMigrator.translateKey("colour").toKey("couleur")
            .fromValue("red").toValue("rouge")
            .fromValue("green").toValue("vert"),
        PrefMigrator.translateKey("vegetable").toKey("legume")
            .fromValue("tomato").toValue("tomate")
            .fromValue("eggplant").toValue("aubergine")
    };

    // Splits a key into two more specialized keys with less qualified values.
    private static final Migration[] KEY_SPLITTER = {
        PrefMigrator.translateKey("fruit").toKey("pear")
            .fromValue("pear_anjou").toValue("anjou")
            .fromValue("pear_bartlett").toValue("bartlett"),
        PrefMigrator.translateKey("fruit").toKey("melon")
            .fromValue("melon_cantaloupe").toValue("cantaloupe")
            .fromValue("melon_watermelon").toValue("watermelon")
    };

    // Merges two keys into one more general key with more qualified values.
    private static final Migration[] KEY_MERGER = {
        PrefMigrator.translateKey("pear").toKey("fruit")
            .fromValue("anjou").toValue("pear_anjou")
            .fromValue("bartlett").toValue("pear_bartlett"),
        PrefMigrator.translateKey("melon").toKey("fruit")
            .fromValue("cantaloupe").toValue("melon_cantaloupe")
            .fromValue("watermelon").toValue("melon_watermelon")
    };

    // Produces new values based on a combination of the values of two keys.
    private static final Migration[] COMBINER = {
        PrefMigrator.combineKeys("red", "blue")
            .withValues(true, true).toPairs("mix", "purple")
            .withValues(true, false).toPairs("mix", "red")
            .withValues(true, null).toPairs("mix", "red")
            .withValues(false, true).toPairs("mix", "blue")
            .withValues(null, true).toPairs("mix", "blue")
    };

    // Uses the value of one key to modify the value of another.
    private static final Migration[] COMBINER_MODIFIER = {
        PrefMigrator.combineKeys("vehicle", "fast")
            .withValues("bicycle", true).toPairs("vehicle", "fast_bicycle")
            .withValues("car", true).toPairs("vehicle", "fast_car")
    };

    @Before public void setUp() throws Exception {
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

    @Test public void renamerShouldRenameKeys() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, RENAMERS);
        assertPrefs(
            "couleur", "red",
            "legume", "eggplant"
        );
    }

    @Test public void translatorShouldTranslateValues() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "couleur", "rouge",
            "legume", "aubergine"
        );
    }

    @Test public void translatorShouldNotTouchUnknownValues() {
        initPrefs(
            "colour", "blue",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "colour", "blue",
            "legume", "aubergine"
        );
    }

    @Test public void renamerSouldNotReplaceExistingNewKeys() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
        PrefMigrator.migrate(prefs, RENAMERS);
        assertPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
    }

    @Test public void translatorShouldNotReplaceExistingNewKeys() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
    }

    @Test public void translatorShouldSplitKeyAccordingToValue() {
        initPrefs("fruit", "pear_anjou");
        PrefMigrator.migrate(prefs, KEY_SPLITTER);
        assertPrefs("pear", "anjou");

        initPrefs("fruit", "melon_watermelon");
        PrefMigrator.migrate(prefs, KEY_SPLITTER);
        assertPrefs("melon", "watermelon");
    }

    @Test public void translatorShouldMergeKeys() {
        initPrefs("pear", "anjou");
        PrefMigrator.migrate(prefs, KEY_MERGER);
        assertPrefs("fruit", "pear_anjou");

        initPrefs("melon", "watermelon");
        PrefMigrator.migrate(prefs, KEY_MERGER);
        assertPrefs("fruit", "melon_watermelon");
    }

    @Test public void combinerShouldCombineKeys() {
        initPrefs("red", true, "blue", false);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("mix", "red");

        initPrefs("red", false, "blue", true);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("mix", "blue");

        initPrefs("red", true, "blue", true);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("mix", "purple");

        initPrefs("red", false);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("red", false);

        initPrefs("red", false, "blue", false);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("red", false, "blue", false);

        initPrefs("red", true);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("mix", "red");

        initPrefs("blue", true);
        PrefMigrator.migrate(prefs, COMBINER);
        assertPrefs("mix", "blue");
    }

    @Test public void combinerShouldModifyKey() {
        initPrefs("vehicle", "bicycle");
        PrefMigrator.migrate(prefs, COMBINER_MODIFIER);
        assertPrefs("vehicle", "bicycle");

        initPrefs("vehicle", "bicycle", "fast", true);
        PrefMigrator.migrate(prefs, COMBINER_MODIFIER);
        assertPrefs("vehicle", "fast_bicycle");

        initPrefs("vehicle", "car");
        PrefMigrator.migrate(prefs, COMBINER_MODIFIER);
        assertPrefs("vehicle", "car");

        initPrefs("vehicle", "car", "fast", true);
        PrefMigrator.migrate(prefs, COMBINER_MODIFIER);
        assertPrefs("vehicle", "fast_car");

        initPrefs("vehicle", "airplane", "fast", true);
        PrefMigrator.migrate(prefs, COMBINER_MODIFIER);
        assertPrefs("vehicle", "airplane", "fast", true);
    }

    @Test public void shouldMigrateGoogleMapSettings() {
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

    @Test public void shouldMigrateMapboxMapSettings() {
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

    @Test public void shouldMigrateOsmMapSettings() {
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

    @Test public void shouldMigrateAdminSettings() {
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
}
