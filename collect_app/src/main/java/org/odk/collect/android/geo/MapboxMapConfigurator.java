package org.odk.collect.android.geo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

import com.google.common.collect.ImmutableSet;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

class MapboxMapConfigurator implements MapConfigurator {
    @Override public boolean isAvailable(Context context) {
        return MapboxUtils.initMapbox() != null;
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(
            R.string.mapbox_unsupported_warning, Build.CPU_ABI));
    }

    @Override public MapFragment createMapFragment(Context context) {
        return MapboxUtils.initMapbox() != null ? new MapboxMapFragment() : null;
    }

    @Override public List<Preference> createPrefs(Context context) {
        return Arrays.asList(
            PrefUtils.createListPref(
                context,
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

    @Override public Set<String> getPrefKeys() {
        return ImmutableSet.of(KEY_MAPBOX_MAP_STYLE, KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(SharedPreferences prefs) {
        Bundle config = new Bundle();
        config.putString(MapboxMapFragment.KEY_STYLE_URL,
            prefs.getString(KEY_MAPBOX_MAP_STYLE, Style.MAPBOX_STREETS));
        config.putString(MapboxMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(KEY_REFERENCE_LAYER, null));
        return config;
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
