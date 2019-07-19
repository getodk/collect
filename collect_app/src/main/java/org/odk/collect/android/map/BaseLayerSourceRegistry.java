package org.odk.collect.android.map;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.map.WmsBaseLayerSource.WmsOption;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.spatial.TileSourceFactory;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASE_LAYER_SOURCE;

/** A static class that defines the set of available base layer sources. */
public class BaseLayerSourceRegistry {
    private BaseLayerSourceRegistry() { }  // prevent instantiation

    private static Option[] OPTIONS = initOptions();

    public static class Option {
        public final String id;  // preference value to store
        public final int labelId;  // string resource ID
        public final BaseLayerSource provider;

        public Option(String id, int labelId, BaseLayerSource provider) {
            this.id = id;
            this.labelId = labelId;
            this.provider = provider;
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
        for (Option option : OPTIONS) {
            if (option.id.equals(id)) {
                return option;
            }
        }
        return OPTIONS[0];
    }

    /** Gets the Option corresponding to the current base_layer_source preference. */
    public static Option getCurrent(Context context) {
        return get(PrefUtils.getSharedPrefs(context).getString(KEY_BASE_LAYER_SOURCE, null));
    }

    /** Asks the currently selected BaseLayerSource to make us a MapFragment. */
    public static MapFragment createMapFragment(Context context) {
        Option option = getCurrent(context);
        return option == null ? null : option.provider.createMapFragment(context);
    }

    /** Gets a list of the IDs of the options, in order. */
    public static String[] getIds() {
        String[] ids = new String[OPTIONS.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = OPTIONS[i].id;
        }
        return ids;
    }

    /** Gets a list of the label string IDs of the options, in order. */
    public static int[] getLabelIds() {
        int[] labelIds = new int[OPTIONS.length];
        for (int i = 0; i < labelIds.length; i++) {
            labelIds[i] = OPTIONS[i].labelId;
        }
        return labelIds;
    }
}
