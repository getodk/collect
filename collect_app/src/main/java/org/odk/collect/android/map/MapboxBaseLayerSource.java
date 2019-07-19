package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PrefUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

public class MapboxBaseLayerSource implements BaseLayerSource {
    @Override public void onSelected() {
        // To keep our APK from getting too big, we decided to include the
        // Mapbox native library only for the most common binary architectures.
        // So, on a small minority of Android devices, the Mapbox SDK will not
        // run; let's warn the user when they choose Mapbox in the settings.
        if (MapboxUtils.initMapbox() == null) {
            MapboxUtils.warnMapboxUnsupported(Collect.getInstance());
        }
    }

    @Override public void addPrefs(PreferenceCategory category) {
        category.addPreference(
            PrefUtils.createListPref(
                category.getContext(),
                KEY_MAPBOX_MAP_STYLE,
                R.string.mapbox_map_style,
                new int[] {
                    R.string.mapbox_map_style_streets,
                    R.string.mapbox_map_style_light,
                    R.string.mapbox_map_style_dark,
                    R.string.mapbox_map_style_satellite,
                    R.string.mapbox_map_style_satellite_streets,
                    R.string.mapbox_map_style_outdoors,
                },
                new String[] {
                    Style.MAPBOX_STREETS,
                    Style.LIGHT,
                    Style.DARK,
                    Style.SATELLITE,
                    Style.SATELLITE_STREETS,
                    Style.OUTDOORS
                }
            )
        );
    }

    @Override public MapFragment createMapFragment(Context context) {
        if (MapboxUtils.initMapbox() == null) {
            MapboxUtils.warnMapboxUnsupported(Collect.getInstance());
            return null;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String styleUrl = prefs.getString(KEY_MAPBOX_MAP_STYLE, Style.MAPBOX_STREETS);
        String referencePath = prefs.getString(KEY_REFERENCE_LAYER, null);
        File referenceLayer = referencePath == null ? null : new File(referencePath);
        return new MapboxMapFragment(styleUrl, referenceLayer);
    }

    @Override public boolean supportsLayer(File file) {
        if (file.getName().endsWith(".mbtiles")) {
            try {
                // MapboxMapFragment supports any file that MbtilesFile can read.
                new MbtilesFile(file);
                return true;
            } catch (MbtilesFile.MbtilesException e) {
                Timber.d(e);
            }
        }
        return false;
    }
}
