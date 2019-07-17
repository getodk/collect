package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import org.odk.collect.android.preferences.PrefUtils;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.io.File;

public class WmsBaseLayerSource implements BaseLayerSource {
    private final String prefKey;
    private final int prefTitleId;
    private final WmsOption[] options;

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
        OnlineTileSourceBase source = options[0].source;
        if (options.length > 1) {
            String value = prefs.getString(prefKey, null);
            for (int i = 0; i < options.length; i++) {
                if (options[i].id.equals(value)) {
                    source = options[i].source;
                }
            }
        }
        return new OsmMapFragment(source);
    }

    @Override public boolean supportsLayer(File file) {
        return false;
    }

    @Override public String getDisplayName(File file) {
        return null;
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
