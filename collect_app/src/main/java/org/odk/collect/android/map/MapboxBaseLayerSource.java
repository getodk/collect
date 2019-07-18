package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceCategory;

import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

public class MapboxBaseLayerSource implements BaseLayerSource {
    // We must hold on to the listener or it will be garbage-collected.
    // See SharedPreferences.registerOnSharedPreferenceChangeListener().
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override public boolean isAvailable(Context context) {
        return MapboxUtils.initMapbox() != null;
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(
            R.string.mapbox_unsupported_warning, Build.CPU_ABI));
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
            return null;
        }
        SharedPreferences prefs = PrefUtils.getSharedPrefs(context);
        MapboxMapFragment map = new MapboxMapFragment();
        setStyleUrlFromPrefs(prefs, map);
        setReferenceLayerFromPrefs(prefs, map);

        listener = (SharedPreferences p, String key) -> {
            if (key.equals(KEY_MAPBOX_MAP_STYLE)) {
                setStyleUrlFromPrefs(prefs, map);
            }
            if (key.equals(KEY_REFERENCE_LAYER)) {
                setReferenceLayerFromPrefs(prefs, map);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return map;
    }

    private void setStyleUrlFromPrefs(SharedPreferences prefs, MapboxMapFragment map) {
        map.setStyleUrl(prefs.getString(KEY_MAPBOX_MAP_STYLE, Style.MAPBOX_STREETS));
    }

    private void setReferenceLayerFromPrefs(SharedPreferences prefs, MapboxMapFragment map) {
        String path = prefs.getString(KEY_REFERENCE_LAYER, null);
        map.setReferenceLayerFile(path != null ? new File(path) : null);
    }

    @Override public boolean supportsLayer(File file) {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.getLayerType(file) != null;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.getName(file);
        return name != null ? name : file.getName();
    }
}
