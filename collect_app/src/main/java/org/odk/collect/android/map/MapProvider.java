package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import org.odk.collect.android.R;
import org.odk.collect.android.map.OsmDroidMapConfigurator.WmsOption;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PrefUtils;

import java.util.Map;
import java.util.WeakHashMap;

import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_STAMEN;
import static org.odk.collect.android.preferences.GeneralKeys.BASE_LAYER_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASE_LAYER_SOURCE;

/** A static class that obtains a MapFragment according to the user's preferences. */
public class MapProvider {
    private MapProvider() { }  // prevent instantiation

    private static final Option[] BASE_LAYER_SOURCE_OPTIONS = initOptions();
    private static final String USGS_URL_BASE =
        "https://basemap.nationalmap.gov/arcgis/rest/services";
    private static final String OSM_COPYRIGHT = "© OpenStreetMap contributors";

    public static class Option {
        public final String id;  // preference value to store
        public final int sourceLabelId;  // string resource ID
        public final MapConfigurator source;

        public Option(String id, int sourceLabelId, MapConfigurator source) {
            this.id = id;
            this.sourceLabelId = sourceLabelId;
            this.source = source;
        }
    }

    /** Defines the available base layer sources, in their order of appearance. */
    private static Option[] initOptions() {
        return new Option[] {
            new Option(BASE_LAYER_SOURCE_GOOGLE, R.string.base_layer_source_google,
                new GoogleMapConfigurator()),
            new Option(BASE_LAYER_SOURCE_MAPBOX, R.string.base_layer_source_mapbox,
                new MapboxMapConfigurator()),
            new Option(BASE_LAYER_SOURCE_OSM, R.string.base_layer_source_osm,
                new OsmDroidMapConfigurator(
                    new WebMapService(
                        "Mapnik", 0, 19, 256, OSM_COPYRIGHT,
                        "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                        "http://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                        "http://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    )
                )
            ),
            new Option(BASE_LAYER_SOURCE_USGS, R.string.base_layer_source_usgs,
                new OsmDroidMapConfigurator(
                    GeneralKeys.KEY_USGS_MAP_STYLE, R.string.usgs_map_style,
                    new WmsOption("topo", R.string.usgs_map_style_topo, new WebMapService(
                        R.string.openmap_usgs_topo, 0, 18, 256, "USGS",
                        USGS_URL_BASE + "/USGSTopo/MapServer/tile/{z}/{y}/{x}"
                    )),
                    new WmsOption("hybrid", R.string.usgs_map_style_hybrid, new WebMapService(
                        R.string.openmap_usgs_sat, 0, 18, 256, "USGS",
                        USGS_URL_BASE + "/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}"
                    )),
                    new WmsOption("imagery", R.string.usgs_map_style_imagery, new WebMapService(
                        R.string.openmap_usgs_img, 0, 18, 256, "USGS",
                        USGS_URL_BASE + "/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}"
                    ))
                )
            ),
            new Option(BASE_LAYER_SOURCE_STAMEN, R.string.base_layer_source_stamen,
                new OsmDroidMapConfigurator(
                    new WebMapService(
                        R.string.openmap_stamen_terrain, 0, 18, 256, OSM_COPYRIGHT,
                        "http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
                    )
                )
            ),
            new Option(BASE_LAYER_SOURCE_CARTO, R.string.base_layer_source_carto,
                new OsmDroidMapConfigurator(
                    GeneralKeys.KEY_CARTO_MAP_STYLE, R.string.carto_map_style,
                    new WmsOption("positron", R.string.carto_map_style_positron, new WebMapService(
                        R.string.openmap_cartodb_positron, 0, 18, 256, OSM_COPYRIGHT,
                        "http://1.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                    )),
                    new WmsOption("dark_matter", R.string.carto_map_style_dark_matter, new WebMapService(
                        R.string.openmap_cartodb_darkmatter, 0, 18, 256, OSM_COPYRIGHT,
                        "http://1.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
                    ))
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
    public static Option getCurrentOption(Context context) {
        return get(PrefUtils.getSharedPrefs(context).getString(KEY_BASE_LAYER_SOURCE, null));
    }

    public static MapConfigurator getCurrentSource(Context context) {
        Option option = getCurrentOption(context);
        return option != null ? option.source : null;
    }

    private static Map<MapFragment, OnSharedPreferenceChangeListener> listenersByMap = new WeakHashMap<>();
    private static Map<MapFragment, MapConfigurator> sourcesByMap = new WeakHashMap<>();

    public static MapFragment createMapFragment(Context context) {
        MapConfigurator source = getCurrentSource(context);
        if (source != null) {
            MapFragment map = source.createMapFragment(context);
            if (map != null) {
                sourcesByMap.put(map, source);
                return map;
            }
            source.showUnavailableMessage(context);
        }
        return null;
    }

    public static void onMapFragmentStart(MapFragment map) {
        Context context = map.getFragment().getContext();
        MapConfigurator source = sourcesByMap.get(map);
        if (source != null) {
            OnSharedPreferenceChangeListener listener = (prefs, key) -> {
                if (source.getPrefKeys().contains(key)) {
                    map.applyConfig(source.buildConfig(prefs));
                }
            };
            SharedPreferences prefs = PrefUtils.getSharedPrefs(context);
            map.applyConfig(source.buildConfig(prefs));
            prefs.registerOnSharedPreferenceChangeListener(listener);
            listenersByMap.put(map, listener);
        }
    }

    public static void onMapFragmentStop(MapFragment map) {
        OnSharedPreferenceChangeListener listener = listenersByMap.get(map);
        if (listener != null) {
            Context context = map.getFragment().getContext();
            SharedPreferences prefs = PrefUtils.getSharedPrefs(context);
            prefs.unregisterOnSharedPreferenceChangeListener(listener);
            listenersByMap.remove(listener);
        }
    }

    /** Gets a list of the IDs of the base layer sources, in order. */
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
