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

    /**
    /** Constructs a base layer that provides just one Web Map Service. */
    public WmsBaseLayerSource(OnlineTileSourceBase source) {
        prefKey = "";
        prefTitleId = 0;
        options = new WmsOption[] {new WmsOption("", 0, source)};
    }

    /**
     * Constructs a base layer that provides a few Web Map Services to choose from.
     * The choice of which Web Map Service will be stored in a string preference.
     */
    public WmsBaseLayerSource(String prefKey, int prefTitleId, WmsOption... options) {
        this.prefKey = prefKey;
        this.prefTitleId = prefTitleId;
        this.options = options;
    }

    @Override public void onSelected() { }

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
        OnlineTileSourceBase source = options[0].source;
        if (options.length > 1) {
            String value = prefs.getString(prefKey, null);
            for (int i = 0; i < options.length; i++) {
                if (options[i].id.equals(value)) {
                    source = options[i].source;
                }
            }
        }
        MapFragment map = new OsmMapFragment(options[0].source);
        String referencePath = prefs.getString(KEY_REFERENCE_LAYER, null);
        map.setReferenceLayer(referencePath == null ? null : new File(referencePath));
        return map;
    }

    @Override public boolean supportsLayer(File path) {
        return false;
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
