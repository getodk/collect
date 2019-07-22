package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.Map;

import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;

/** Migrates old preference keys and values to new ones. */
public class PrefMigrator {

    private PrefMigrator() { } // prevent instantiation

    // Convention below: the new (destination) key comes first, followed by
    // old keys; the new value comes first, followed by old values.
    public static final Migration[] MIGRATIONS = {
        new Translator(KEY_BASEMAP_SOURCE, "map_sdk_behavior")
            .withValue(BASEMAP_SOURCE_GOOGLE, "google_maps")
            .withValue(BASEMAP_SOURCE_OSM, "osmdroid")
            .withValue(BASEMAP_SOURCE_MAPBOX, "mapbox_maps"),

        new Translator(KEY_GOOGLE_MAP_STYLE, "map_basemap_behavior")
            .withValue(GoogleMap.MAP_TYPE_NORMAL, "streets")
            .withValue(GoogleMap.MAP_TYPE_TERRAIN, "terrain")
            .withValue(GoogleMap.MAP_TYPE_HYBRID, "hybrid")
            .withValue(GoogleMap.MAP_TYPE_SATELLITE, "satellite"),

        new Translator(KEY_MAPBOX_MAP_STYLE, "map_basemap_behavior")
            .withValue(Style.MAPBOX_STREETS, "mapbox_streets")
            .withValue(Style.LIGHT, "mapbox_light")
            .withValue(Style.DARK, "mapbox_dark")
            .withValue(Style.SATELLITE, "mapbox_satellite")
            .withValue(Style.SATELLITE_STREETS, "mapbox_satellite_streets")
            .withValue(Style.OUTDOORS, "mapbox_outdoors"),
    };

    public static void migrate(SharedPreferences prefs, Migration... migrations) {
        for (Migration migration : migrations) {
            migration.apply(prefs);
        }
    }

    public static void migrateSharedPrefs() {
        migrate(PrefUtils.getSharedPrefs(), MIGRATIONS);
    }

    public interface Migration {
        void apply(SharedPreferences prefs);
    }

    /**
     * A migration that moves the value of an old preference key over to a new key.
     * Migrates only if the old key is set and the new key is not set.
     */
    public static class Copier implements Migration {
        protected String newKey;
        protected String oldKey;

        public Copier(String newKey, String oldKey) {
            this.newKey = newKey;
            this.oldKey = oldKey;
        }

        public void apply(SharedPreferences prefs) {
            if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
                replace(prefs, new String[] {oldKey},
                    newKey, prefs.getAll().get(oldKey));
            }
        }
    }

    /**
     * A migration that replaces an old preference key with a new key, translating
     * specific old values to specific new values.  Migrates only if the old key
     * is set and the new key is not set.  The old key is replaced with the new
     * key ONLY if the old value is found among the specified translations.
     */
    public static class Translator extends Copier {
        protected Map<Object, Object> translatedValues = new HashMap<>();

        public Translator(String newKey, String oldKey) {
            super(newKey, oldKey);
        }

        public Translator withValue(Object newValue, Object oldValue) {
            translatedValues.put(oldValue, newValue);
            return this;
        }

        public void apply(SharedPreferences prefs) {
            if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
                Object oldValue = prefs.getAll().get(oldKey);
                if (translatedValues.containsKey(oldValue)) {
                    replace(prefs, new String[] {oldKey},
                        newKey, translatedValues.get(oldValue));
                }
            }
        }
    }

    /** Replaces one or more old keys with a new key set to a value of arbitrary type. */
    @SuppressLint("ApplySharedPref")
    public static void replace(
        SharedPreferences prefs, String[] oldKeys, String newKey, Object newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        if (newValue instanceof String) {
            editor.putString(newKey, (String) newValue);
        } else if (newValue instanceof Boolean) {
            editor.putBoolean(newKey, (Boolean) newValue);
        } else if (newValue instanceof Long) {
            editor.putLong(newKey, (Long) newValue);
        } else if (newValue instanceof Integer) {
            editor.putInt(newKey, (Integer) newValue);
        } else if (newValue instanceof Float) {
            editor.putFloat(newKey, (Float) newValue);
        }
        for (String oldKey : oldKeys) {
            if (!oldKey.equals(newKey)) {
                editor.remove(oldKey);
            }
        }
        editor.commit();
    }
}
