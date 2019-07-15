package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;

public class MapboxBaseLayerSource implements BaseLayerSource {
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String styleUrl = prefs.getString(KEY_MAPBOX_MAP_STYLE, Style.MAPBOX_STREETS);
        return new MapboxMapFragment(styleUrl);
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
