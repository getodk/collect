package org.odk.collect.android.map;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.map.WmsBaseLayerSource.WmsOption;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.spatial.TileSourceFactory;

import java.io.File;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASE_LAYER_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

/** A static class that configures a MapFragment according to the user's preferences. */
public class MapConfigurator {
    private MapConfigurator() { }  // prevent instantiation

    private static final Option[] BASE_LAYER_SOURCE_OPTIONS = initOptions();

    public static class Option {
        public final String id;  // preference value to store
        public final int sourceLabelId;  // string resource ID
        public final BaseLayerSource source;

        public Option(String id, int sourceLabelId, BaseLayerSource source) {
            this.id = id;
            this.sourceLabelId = sourceLabelId;
            this.source = source;
        }
    }

    /** Defines the available base layer sources, in their order of appearance. */
    private static Option[] initOptions() {
        TileSourceFactory factory = new TileSourceFactory(Collect.getInstance().getApplicationContext());
        return new Option[] {
            new Option("google", R.string.base_layer_source_google,
                new GoogleBaseLayerSource()),
            new Option("mapbox", R.string.base_layer_source_mapbox,
                new MapboxBaseLayerSource()),
            new Option("osm", R.string.base_layer_source_osm,
                new WmsBaseLayerSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
            ),
            new Option("usgs", R.string.base_layer_source_usgs,
                new WmsBaseLayerSource(
                    GeneralKeys.KEY_USGS_MAP_STYLE, R.string.usgs_map_style,
                    new WmsOption("topo", R.string.usgs_map_style_topo, factory.getUSGSTopo()),
                    new WmsOption("hybrid", R.string.usgs_map_style_hybrid, factory.getUsgsSat()),
                    new WmsOption("imagery", R.string.usgs_map_style_imagery, factory.getUsgsImg())
                )
            ),
            new Option("stamen", R.string.base_layer_source_stamen,
                new WmsBaseLayerSource(factory.getStamenTerrain())
            ),
            new Option("carto", R.string.base_layer_source_carto,
                new WmsBaseLayerSource(
                    GeneralKeys.KEY_CARTO_MAP_STYLE, R.string.carto_map_style,
                    new WmsOption("positron", R.string.carto_map_style_positron, factory.getCartoDbPositron()),
                    new WmsOption("dark_matter", R.string.carto_map_style_dark_matter, factory.getCartoDbDarkMatter())
                )
            )
        };
    }

    /** Gets the Option with the given ID, or the first option if the ID is unknown. */
    public static Option get(String id) {
        for (Option option : BASE_LAYER_SOURCE_OPTIONS) {
            if (option.id.equals(id)) {
                return option;
            }
        }
        return BASE_LAYER_SOURCE_OPTIONS[0];
    }

    /** Gets the Option corresponding to the current base_layer_source preference. */
    public static Option getCurrent(Context context) {
        return get(PrefUtils.getSharedPrefs(context).getString(KEY_BASE_LAYER_SOURCE, null));
    }

    /** Creates a MapFragment with the selected base layer and reference layer. */
    public static MapFragment createMapFragment(Context context) {
        Option option = getCurrent(context);
        if (option == null) {
            return null;
        }
        MapFragment map = option.source.createMapFragment(context);
        if (map == null) {
            option.source.showUnavailableMessage(context);
            return null;
        }
        String referencePath = PrefUtils.getSharedPrefs(context).getString(KEY_REFERENCE_LAYER, null);
        map.setReferenceLayerFile(referencePath == null ? null : new File(referencePath));
        return map;
    }

    /** Gets a list of the IDs of the base layer source, in order. */
    public static String[] getIds() {
        String[] ids = new String[BASE_LAYER_SOURCE_OPTIONS.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = BASE_LAYER_SOURCE_OPTIONS[i].id;
        }
        return ids;
    }

    /** Gets a list of the label string IDs of the base layer sources, in order. */
    public static int[] getLabelIds() {
        int[] labelIds = new int[BASE_LAYER_SOURCE_OPTIONS.length];
        for (int i = 0; i < labelIds.length; i++) {
            labelIds[i] = BASE_LAYER_SOURCE_OPTIONS[i].sourceLabelId;
        }
        return labelIds;
    }
}
