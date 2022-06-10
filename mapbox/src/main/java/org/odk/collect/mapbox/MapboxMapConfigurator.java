package org.odk.collect.mapbox;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import com.google.common.collect.ImmutableSet;
import com.mapbox.maps.Style;

import org.odk.collect.androidshared.ui.PrefUtils;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.maps.MapConfigurator;
import org.odk.collect.maps.layers.MbtilesFile;
import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.shared.settings.Settings;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MapboxMapConfigurator implements MapConfigurator {
    private final String prefKey;
    private final int sourceLabelId;
    private final MapboxUrlOption[] options;

    /** Constructs a configurator with a few Mapbox style URL options to choose from. */
    public MapboxMapConfigurator() {
        this.prefKey = KEY_MAPBOX_MAP_STYLE;
        this.sourceLabelId = R.string.basemap_source_mapbox;
        this.options = new MapboxUrlOption[]{
                new MapboxUrlOption(Style.MAPBOX_STREETS, R.string.streets),
                new MapboxUrlOption(Style.LIGHT, R.string.light),
                new MapboxUrlOption(Style.DARK, R.string.dark),
                new MapboxUrlOption(Style.SATELLITE, R.string.satellite),
                new MapboxUrlOption(Style.SATELLITE_STREETS, R.string.hybrid),
                new MapboxUrlOption(Style.OUTDOORS, R.string.outdoors)
        };
    }

    @Override public boolean isAvailable(Context context) {
        // If the app builds that means mapbox is available
        return true;
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context, context.getString(
            R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
    }

    @Override public List<Preference> createPrefs(Context context, Settings settings) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = options[i].url;
        }
        String prefTitle = context.getString(
            R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(PrefUtils.createListPref(
            context, prefKey, prefTitle, labelIds, values, settings
        ));
    }

    @Override public Set<String> getPrefKeys() {
        return prefKey.isEmpty() ? ImmutableSet.of(ProjectKeys.KEY_REFERENCE_LAYER) :
            ImmutableSet.of(prefKey, ProjectKeys.KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(Settings prefs) {
        Bundle config = new Bundle();
        config.putString(MapboxMapFragment.KEY_STYLE_URL,
            prefs.getString(ProjectKeys.KEY_MAPBOX_MAP_STYLE));
        config.putString(MapboxMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(ProjectKeys.KEY_REFERENCE_LAYER));
        return config;
    }

    @Override public boolean supportsLayer(File file) {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.readLayerType(file) != null;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    static class MapboxUrlOption {
        final String url;
        final int labelId;

        MapboxUrlOption(String url, int labelId) {
            this.url = url;
            this.labelId = labelId;
        }
    }
}
