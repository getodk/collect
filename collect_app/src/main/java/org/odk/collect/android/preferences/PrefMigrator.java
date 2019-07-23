package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_STAMEN;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USGS_MAP_STYLE;

/** Migrates old preference keys and values to new ones. */
public class PrefMigrator {

    private PrefMigrator() { } // prevent instantiation

    static final Migration[] MIGRATIONS = {
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
    };

    static final Migration[] ADMIN_MIGRATIONS = {
        // When either the map SDK or the basemap selection were previously
        // hidden, we want to hide the entire Maps preference screen.
        translateKey("show_map_sdk").toKey("maps")
            .fromValue(false).toValue(false),
        translateKey("show_map_basemap").toKey("maps")
            .fromValue(false).toValue(false)
    };

    public static void migrate(SharedPreferences prefs, Migration... migrations) {
        for (Migration migration : migrations) {
            migration.apply(prefs);
        }
    }

    public static void migrateSharedPrefs() {
        migrate(PrefUtils.getSharedPrefs(), MIGRATIONS);
        migrate(PrefUtils.getAdminSharedPrefs(), ADMIN_MIGRATIONS);
    }

    public interface Migration {
        void apply(SharedPreferences prefs);
    }

    /**
     * A migration that moves the value of an old preference key over to a new key.
     * Removes the old key and writes the new key ONLY if the old key is set and
     * the new key is not set.  Example:
     *         renameKey("color").toKey("couleur")
     */
    static KeyRenamer renameKey(String oldKey) {
        return new KeyRenamer(oldKey);
    }

    static class KeyRenamer implements Migration {
        String oldKey;
        String newKey;

        KeyRenamer(String oldKey) {
            this.oldKey = oldKey;
        }

        public KeyRenamer toKey(String newKey) {
            this.newKey = newKey;
            return this;
        }

        public void apply(SharedPreferences prefs) {
            if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
                Object value = prefs.getAll().get(oldKey);
                replace(prefs, oldKey, newKey, value);
            }
        }
    }

    /**
     * A migration that replaces an old preference key with a new key, translating
     * specific old values to specific new values.  Removes the old key and writes
     * the new key ONLY if the old key's value exactly matches one of the values
     * passed to fromValue() and the new key is not set.  Example:
     *         translateKey("color").toKey("couleur")
     *             .fromValue("red").toValue("rouge")
     *             .fromValue("yellow").toValue("jaune")
     */
    static KeyTranslator translateKey(String oldKey) {
        return new KeyTranslator(oldKey);
    }

    static class KeyTranslator implements Migration {
        String oldKey;
        String newKey;
        Object tempOldValue;
        Map<Object, Object> translatedValues = new HashMap<>();

        KeyTranslator(String oldKey) {
            this.oldKey = oldKey;
        }

        public KeyTranslator toKey(String newKey) {
            this.newKey = newKey;
            return this;
        }

        public KeyTranslator fromValue(Object oldValue) {
            this.tempOldValue = oldValue;
            return this;
        }

        public KeyTranslator toValue(Object newValue) {
            translatedValues.put(tempOldValue, newValue);
            return this;
        }

        public void apply(SharedPreferences prefs) {
            if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
                Object oldValue = prefs.getAll().get(oldKey);
                Object newValue = translatedValues.get(oldValue);
                if (newValue != null) {
                    replace(prefs, oldKey, newKey, newValue);
                }
            }
        }
    }

    /**
     * A migration that combines multiple keys by looking for specific sets of
     * old values across multiple keys, and replacing them with new key-value pairs.
     * Removes the old keys and writes the new key-value pairs ONLY if all the
     * values of the old keys match the set of values passed to withValues().
     * New key-value pairs MAY overwrite existing keys.  Example:
     *         combineKeys("colour", "op")
     *             .withValues("red", "lighten")
     *                 .toPairs("colour", "pink")  // only if hue = red AND op = lighten
     *             .withValues("yellow", "darken")
     *                 .toPairs("color", "brown")  // only if hue = yellow AND op = darken
     */
    static KeyCombiner combineKeys(String... oldKeys) {
        return new KeyCombiner(oldKeys);
    }

    static class KeyCombiner implements Migration {
        String[] oldKeys;
        Object[] tempOldValueArray;
        List<Object[]> oldValueArrays = new ArrayList<>();
        List<Pair[]> newPairArrays = new ArrayList<>();

        KeyCombiner(String... oldKeys) {
            this.oldKeys = oldKeys;
        }

        public KeyCombiner withValues(Object... oldValues) {
            tempOldValueArray = oldValues;
            return this;
        }

        public KeyCombiner toPairs(Object... keysAndValues) {
            oldValueArrays.add(tempOldValueArray);
            newPairArrays.add(asPairs(keysAndValues));
            return this;
        }

        public void apply(SharedPreferences prefs) {
            Map<String, ?> prefMap = prefs.getAll();
            Object[] oldValues = new Object[oldKeys.length];
            for (int i = 0; i < oldKeys.length; i++) {
                oldValues[i] = prefMap.get(oldKeys[i]);
            }
            for (int i = 0; i < oldValueArrays.size(); i++) {
                if (Arrays.equals(oldValues, oldValueArrays.get(i))) {
                    replace(prefs, oldKeys, newPairArrays.get(i));
                }
            }
        }
    }

    /** Removes an old key and sets a new key. */
    @SuppressLint("ApplySharedPref")
    private static void replace(SharedPreferences prefs, String oldKey, String newKey, Object newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(oldKey);
        put(editor, newKey, newValue);
        editor.commit();
    }

    /** Removes one or more old keys, then adds one or more new key-value pairs. */
    @SuppressLint("ApplySharedPref")
    private static void replace(SharedPreferences prefs, String[] oldKeys, Pair... newPairs) {
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : oldKeys) {
            editor.remove(key);
        }
        for (Pair pair : newPairs) {
            put(editor, pair.key, pair.value);
        }
        editor.commit();
    }

    /** Writes a key with a value of varying type to a SharedPreferences.Editor. */
    private static void put(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
    }

    /** A single preference setting, consisting of a String key and a value of varying type. */
    private static class Pair {
        final String key;
        final Object value;

        Pair(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    /** Converts an array of alternating keys and values into an array of Pairs. */
    private static Pair[] asPairs(Object... args) {
        Pair[] pairs = new Pair[args.length / 2];
        for (int i = 0; i * 2 + 1 < args.length; i++) {
            pairs[i] = new Pair((String) args[i * 2], args[i * 2 + 1]);
        }
        return pairs;
    }
}
