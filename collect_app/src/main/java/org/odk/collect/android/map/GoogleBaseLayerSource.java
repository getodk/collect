package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.R;
import org.odk.collect.android.map.MbtilesFile.LayerType;
import org.odk.collect.android.preferences.PrefUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

public class GoogleBaseLayerSource implements BaseLayerSource {
    @Override public void onSelected() { }

    @Override public void addPrefs(PreferenceCategory category) {
        Context context = category.getContext();
        category.addPreference(
            PrefUtils.createListPref(
                context,
                KEY_GOOGLE_MAP_STYLE,
                R.string.google_map_style,
                new int[] {
                    R.string.google_map_style_streets,
                    R.string.google_map_style_terrain,
                    R.string.google_map_style_hybrid,
                    R.string.google_map_style_satellite,
                },
                new String[] {
                    Integer.toString(GoogleMap.MAP_TYPE_NORMAL),
                    Integer.toString(GoogleMap.MAP_TYPE_TERRAIN),
                    Integer.toString(GoogleMap.MAP_TYPE_HYBRID),
                    Integer.toString(GoogleMap.MAP_TYPE_SATELLITE)
                }
            )
        );
    }

    @Override public MapFragment createMapFragment(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int mapType = Integer.valueOf(prefs.getString(
            KEY_GOOGLE_MAP_STYLE, Integer.toString(GoogleMap.MAP_TYPE_NORMAL)));
        String referencePath = prefs.getString(KEY_REFERENCE_LAYER, null);
        File referenceLayer = referencePath == null ? null : new File(referencePath);
        return new GoogleMapFragment(mapType, referenceLayer);
    }

    @Override public boolean supportsLayer(File file) {
        if (file.getName().endsWith(".mbtiles")) {
            try {
                // GoogleMapFragment supports only raster tiles.
                return new MbtilesFile(file).getLayerType() == LayerType.RASTER;
            } catch (MbtilesFile.MbtilesException e) {
                Timber.d(e);
            }
        }
        return false;
    }
}
