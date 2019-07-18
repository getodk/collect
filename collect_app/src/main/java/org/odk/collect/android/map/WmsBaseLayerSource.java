package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import org.odk.collect.android.preferences.PrefUtils;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.io.File;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

public class WmsBaseLayerSource implements BaseLayerSource {
    private final String prefKey;
    private final int prefTitleId;
    private final WmsOption[] options;

    // We must hold on to the listener or it will be garbage-collected.
    // See SharedPreferences.registerOnSharedPreferenceChangeListener().
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    /** Constructs a base layer source that renders one Web Map Service. */
    public WmsBaseLayerSource(OnlineTileSourceBase source) {
        prefKey = "";
        prefTitleId = 0;
        options = new WmsOption[] {new WmsOption("", 0, source)};
    }

    /**
     * Constructs a base layer source that offers a few Web Map Services to choose from.
     * The choice of which Web Map Service will be stored in a string preference.
     */
    public WmsBaseLayerSource(String prefKey, int prefTitleId, WmsOption... options) {
        this.prefKey = prefKey;
        this.prefTitleId = prefTitleId;
        this.options = options;
    }

    @Override public boolean isAvailable(Context context) {
        // OSMdroid is always supported, as far as we know.
        return true;
    }

    @Override public void showUnavailableMessage(Context context) { }

    @Override public void addPrefs(PreferenceCategory category) {
        if (options.length > 1) {
            int[] labelIds = new int[options.length];
            String[] values = new String[options.length];
            int i = 0;
            for (WmsOption option : options) {
                labelIds[i] = option.labelId;
                values[i] = option.id;
                i++;
            }
            category.addPreference(PrefUtils.createListPref(
                category.getContext(), prefKey, prefTitleId, labelIds, values));
        }
    }

    @Override public MapFragment createMapFragment(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        OsmMapFragment map = new OsmMapFragment();
        setTileSourceFromPrefs(prefs, map);
        setReferenceLayerFromPrefs(prefs, map);

        listener = (SharedPreferences p, String key) -> {
            if (key.equals(prefKey)) {
                setTileSourceFromPrefs(prefs, map);
            }
            if (key.equals(KEY_REFERENCE_LAYER)) {
                setReferenceLayerFromPrefs(prefs, map);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return map;
    }

    private void setTileSourceFromPrefs(SharedPreferences prefs, OsmMapFragment map) {
        if (options.length == 1) {
            map.setTileSource(options[0].source);
            return;
        } else {
            String value = prefs.getString(prefKey, null);
            for (int i = 0; i < options.length; i++) {
                if (options[i].id.equals(value)) {
                    map.setTileSource(options[i].source);
                    return;
                }
            }
        }
    }

    private void setReferenceLayerFromPrefs(SharedPreferences prefs, OsmMapFragment map) {
        String path = prefs.getString(KEY_REFERENCE_LAYER, null);
        map.setReferenceLayerFile(path != null ? new File(path) : null);
    }

    @Override public boolean supportsLayer(File file) {
        // OSMdroid supports only raster tiles.
        return MbtilesFile.getLayerType(file) == MbtilesFile.LayerType.RASTER;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.getName(file);
        return name != null ? name : file.getName();
    }

    public static class WmsOption {
        final String id;
        final int labelId;
        final OnlineTileSourceBase source;

        WmsOption(String id, int labelId, OnlineTileSourceBase source) {
            this.id = id;
            this.labelId = labelId;
            this.source = source;
        }
    }
}
