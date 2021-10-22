package org.odk.collect.android.geo;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import com.google.common.collect.ImmutableSet;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.shared.Settings;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_REFERENCE_LAYER;

class OsmDroidMapConfigurator implements MapConfigurator {
    private final String prefKey;
    private final int sourceLabelId;
    private final WmsOption[] options;

    /** Constructs a configurator that renders just one Web Map Service. */
    OsmDroidMapConfigurator(WebMapService service) {
        prefKey = "";
        sourceLabelId = 0;
        options = new WmsOption[] {new WmsOption("", 0, service)};
    }

    /**
     * Constructs a configurator that offers a few Web Map Services to choose from.
     * The choice of which Web Map Service will be stored in a string preference.
     */
    OsmDroidMapConfigurator(String prefKey, int sourceLabelId, WmsOption... options) {
        this.prefKey = prefKey;
        this.sourceLabelId = sourceLabelId;
        this.options = options;
    }

    @Override public boolean isAvailable(Context context) {
        // OSMdroid is always supported, as far as we know.
        return true;
    }

    @Override public void showUnavailableMessage(Context context) { }

    @Override public MapFragment createMapFragment(Context context) {
        return new OsmDroidMapFragment();
    }

    @Override public List<Preference> createPrefs(Context context) {
        if (options.length > 1) {
            int[] labelIds = new int[options.length];
            String[] values = new String[options.length];
            for (int i = 0; i < options.length; i++) {
                labelIds[i] = options[i].labelId;
                values[i] = options[i].id;
            }
            String prefTitle = context.getString(
                R.string.map_style_label, context.getString(sourceLabelId));
            return Collections.singletonList(PrefUtils.createListPref(
                context, prefKey, prefTitle, labelIds, values
            ));
        }
        return Collections.emptyList();
    }

    @Override public Collection<String> getPrefKeys() {
        return prefKey.isEmpty() ? ImmutableSet.of(KEY_REFERENCE_LAYER) :
            ImmutableSet.of(prefKey, KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(Settings prefs) {
        Bundle config = new Bundle();
        if (options.length == 1) {
            config.putSerializable(OsmDroidMapFragment.KEY_WEB_MAP_SERVICE, options[0].service);
        } else {
            String value = prefs.getString(prefKey);
            for (int i = 0; i < options.length; i++) {
                if (options[i].id.equals(value)) {
                    config.putSerializable(OsmDroidMapFragment.KEY_WEB_MAP_SERVICE, options[i].service);
                }
            }
        }
        config.putString(OsmDroidMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(KEY_REFERENCE_LAYER));
        return config;
    }

    @Override public boolean supportsLayer(File file) {
        // OSMdroid supports only raster tiles.
        return MbtilesFile.readLayerType(file) == MbtilesFile.LayerType.RASTER;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    static class WmsOption {
        final String id;
        final int labelId;
        final WebMapService service;

        WmsOption(String id, int labelId, WebMapService service) {
            this.id = id;
            this.labelId = labelId;
            this.service = service;
        }
    }
}
