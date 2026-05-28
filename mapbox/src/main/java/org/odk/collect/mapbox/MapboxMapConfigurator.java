package org.odk.collect.mapbox;

import android.content.Context;

import androidx.preference.Preference;

import com.mapbox.maps.Style;

import org.odk.collect.androidshared.system.OpenGLVersionChecker;
import org.odk.collect.androidshared.ui.PrefUtils;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.maps.MapConfigurator;
import org.odk.collect.maps.layers.MbtilesFile;
import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.shared.settings.Settings;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MapboxMapConfigurator implements MapConfigurator {
    private final int sourceLabelId = org.odk.collect.strings.R.string.basemap_source_mapbox;
    private final MapboxUrlOption[] options = {
            new MapboxUrlOption(Style.MAPBOX_STREETS, org.odk.collect.strings.R.string.streets),
            new MapboxUrlOption(Style.LIGHT, org.odk.collect.strings.R.string.light),
            new MapboxUrlOption(Style.DARK, org.odk.collect.strings.R.string.dark),
            new MapboxUrlOption(Style.SATELLITE, org.odk.collect.strings.R.string.satellite),
            new MapboxUrlOption(Style.SATELLITE_STREETS, org.odk.collect.strings.R.string.hybrid),
            new MapboxUrlOption(Style.OUTDOORS, org.odk.collect.strings.R.string.outdoors)
    };

    @Override public boolean isAvailable(Context context) {
        /*
         * The Mapbox SDK for Android requires OpenGL ES version 3.
         * See: https://github.com/mapbox/mapbox-maps-android/blob/main/CHANGELOG.md#1100-november-29-2023
         */
        return OpenGLVersionChecker.isOpenGLv3Supported(context);
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(org.odk.collect.strings.R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
    }

    @Override public List<Preference> createPrefs(Context context, Settings settings) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = options[i].url;
        }
        String prefTitle = context.getString(
            org.odk.collect.strings.R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(PrefUtils.createListPref(
            context, ProjectKeys.KEY_MAPBOX_MAP_STYLE, prefTitle, labelIds, values, settings
        ));
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
