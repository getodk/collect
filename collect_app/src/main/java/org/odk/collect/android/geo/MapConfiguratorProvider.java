package org.odk.collect.android.geo;

import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.application.MapboxClassInstanceCreator;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.googlemaps.GoogleMapConfigurator;
import org.odk.collect.maps.MapConfigurator;

import java.util.ArrayList;
import java.util.List;

public class MapConfiguratorProvider {

    private static SourceOption[] sourceOptions;

    private MapConfiguratorProvider() {

    }

    /**
     * In the preference UI, the available basemaps are organized into "sources"
     * to make them easier to find.  This defines the basemap sources and the
     * basemap options available under each one, in their order of appearance.
     */
    public static void initOptions(Context context) {
        if (sourceOptions != null) {
            return;
        }

        ArrayList<SourceOption> sourceOptions = new ArrayList<>();

        GoogleMapConfigurator googleMapsConfigurator = new GoogleMapConfigurator();
        if (googleMapsConfigurator.isAvailable(context)) {
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_GOOGLE, org.odk.collect.strings.R.string.basemap_source_google,
                    googleMapsConfigurator
            ));
        }

        if (isMapboxSupported()) {
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_MAPBOX, org.odk.collect.strings.R.string.basemap_source_mapbox,
                    MapboxClassInstanceCreator.createMapboxMapConfigurator(BASEMAP_SOURCE_MAPBOX)
            ));

            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_OSM, org.odk.collect.strings.R.string.basemap_source_osm,
                    MapboxClassInstanceCreator.createMapboxMapConfigurator(BASEMAP_SOURCE_OSM)
            ));
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_USGS, org.odk.collect.strings.R.string.basemap_source_usgs,
                    MapboxClassInstanceCreator.createMapboxMapConfigurator(BASEMAP_SOURCE_USGS)
            ));
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_CARTO, org.odk.collect.strings.R.string.basemap_source_carto,
                    MapboxClassInstanceCreator.createMapboxMapConfigurator(BASEMAP_SOURCE_CARTO)
            ));
        }

        initOptions(sourceOptions);
    }

    public static void initOptions(List<SourceOption> sourceOptions) {
        MapConfiguratorProvider.sourceOptions = sourceOptions.toArray(new SourceOption[]{});
    }

    /** Gets the currently selected MapConfigurator. */
    public static @NonNull
    MapConfigurator getConfigurator() {
        return getOption(null).cftor;
    }

    /**
     * Gets the MapConfigurator for the SourceOption with the given id, or the
     * currently selected MapConfigurator if id is null.
     */
    public static @NonNull MapConfigurator getConfigurator(String id) {
        return getOption(id).cftor;
    }

    /** Gets a list of the IDs of the basemap sources, in order. */
    public static String[] getIds() {
        String[] ids = new String[sourceOptions.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = sourceOptions[i].id;
        }
        return ids;
    }

    /** Gets a list of the label string IDs of the basemap sources, in order. */
    public static int[] getLabelIds() {
        int[] labelIds = new int[sourceOptions.length];
        for (int i = 0; i < labelIds.length; i++) {
            labelIds[i] = sourceOptions[i].labelId;
        }
        return labelIds;
    }

    private static boolean isMapboxSupported() {
        return MapboxClassInstanceCreator.isMapboxAvailable();
    }

    /**
     * Gets the SourceOption with the given id, or the currently selected option
     * if id is null, or the first option if the id is unknown.  Never null.
     */
    private static @NonNull SourceOption getOption(String id) {
        if (id == null) {
            id = DaggerUtils.getComponent(getApplication()).settingsProvider().getUnprotectedSettings().getString(KEY_BASEMAP_SOURCE);
        }
        for (SourceOption option : sourceOptions) {
            if (option.id.equals(id)) {
                return option;
            }
        }

        return sourceOptions[0];
    }

    private static Collect getApplication() {
        return Collect.getInstance();
    }

    public static class SourceOption {
        private final String id;  // preference value to store
        private final int labelId;  // string resource ID
        private final MapConfigurator cftor;

        public SourceOption(String id, int labelId, MapConfigurator cftor) {
            this.id = id;
            this.labelId = labelId;
            this.cftor = cftor;
        }
    }
}
