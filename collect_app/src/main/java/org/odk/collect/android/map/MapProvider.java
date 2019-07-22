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

import androidx.annotation.NonNull;

import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_STAMEN;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;

/**
 * A static class that obtains a MapFragment according to the user's preferences.
 * This is the top-level class that should be used by the rest of the application.
 * The available options on the Maps preferences screen are also defined here.
 */
public class MapProvider {
    private MapProvider() { }  // prevent instantiation

    private static final SourceOption[] SOURCE_OPTIONS = initOptions();
    private static final String USGS_URL_BASE =
        "https://basemap.nationalmap.gov/arcgis/rest/services";
    private static final String OSM_COPYRIGHT = "© OpenStreetMap contributors";

    public static class SourceOption {
        public final String id;  // preference value to store
        public final int labelId;  // string resource ID
        public final MapConfigurator cftor;

        public SourceOption(String id, int labelId, MapConfigurator cftor) {
            this.id = id;
            this.labelId = labelId;
            this.cftor = cftor;
        }
    }

    /**
     * In the preference UI, the available basemaps are organized into "sources"
     * to make them easier to find.  This defines the basemap sources and the
     * basemap options available under each one, in their order of appearance.
     */
    private static SourceOption[] initOptions() {
        return new SourceOption[] {
            new SourceOption(BASEMAP_SOURCE_GOOGLE, R.string.basemap_source_google,
                new GoogleMapConfigurator()),
            new SourceOption(BASEMAP_SOURCE_MAPBOX, R.string.basemap_source_mapbox,
                new MapboxMapConfigurator()),
            new SourceOption(BASEMAP_SOURCE_OSM, R.string.basemap_source_osm,
                new OsmDroidMapConfigurator(
                    new WebMapService(
                        "Mapnik", 0, 19, 256, OSM_COPYRIGHT,
                        "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                        "http://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                        "http://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    )
                )
            ),
            new SourceOption(BASEMAP_SOURCE_USGS, R.string.basemap_source_usgs,
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
            new SourceOption(BASEMAP_SOURCE_STAMEN, R.string.basemap_source_stamen,
                new OsmDroidMapConfigurator(
                    new WebMapService(
                        R.string.openmap_stamen_terrain, 0, 18, 256, OSM_COPYRIGHT,
                        "http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
                    )
                )
            ),
            new SourceOption(BASEMAP_SOURCE_CARTO, R.string.basemap_source_carto,
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

    /** Gets the SourceOption with the given ID, or the first option if the ID is unknown. */
    public static @NonNull SourceOption getOption(String id) {
        for (SourceOption option : SOURCE_OPTIONS) {
            if (option.id.equals(id)) {
                return option;
            }
        }
        return SOURCE_OPTIONS[0];
    }

    /** Gets the currently selected SourceOption. */
    public static @NonNull SourceOption getOption() {
        return getOption(PrefUtils.getSharedPrefs().getString(KEY_BASEMAP_SOURCE, null));
    }

    /** Gets the currently selected MapConfigurator. */
    public static @NonNull MapConfigurator getConfigurator() {
        return getOption().cftor;
    }

    private static Map<MapFragment, OnSharedPreferenceChangeListener> listenersByMap = new WeakHashMap<>();
    private static Map<MapFragment, MapConfigurator> sourcesByMap = new WeakHashMap<>();

    public static MapFragment createMapFragment(Context context) {
        MapConfigurator cftor = getConfigurator();
        if (cftor != null) {
            MapFragment map = cftor.createMapFragment(context);
            if (map != null) {
                sourcesByMap.put(map, cftor);
                return map;
            }
            cftor.showUnavailableMessage(context);
        }
        return null;
    }

    public static void onMapFragmentStart(MapFragment map) {
        MapConfigurator cftor = sourcesByMap.get(map);
        if (cftor != null) {
            OnSharedPreferenceChangeListener listener = (prefs, key) -> {
                if (cftor.getPrefKeys().contains(key)) {
                    map.applyConfig(cftor.buildConfig(prefs));
                }
            };
            SharedPreferences prefs = PrefUtils.getSharedPrefs();
            map.applyConfig(cftor.buildConfig(prefs));
            prefs.registerOnSharedPreferenceChangeListener(listener);
            listenersByMap.put(map, listener);
        }
    }

    public static void onMapFragmentStop(MapFragment map) {
        OnSharedPreferenceChangeListener listener = listenersByMap.get(map);
        if (listener != null) {
            SharedPreferences prefs = PrefUtils.getSharedPrefs();
            prefs.unregisterOnSharedPreferenceChangeListener(listener);
            listenersByMap.remove(map);
        }
    }

    /** Gets a list of the IDs of the basemap sources, in order. */
    public static String[] getIds() {
        String[] ids = new String[SOURCE_OPTIONS.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = SOURCE_OPTIONS[i].id;
        }
        return ids;
    }

    /** Gets a list of the label string IDs of the basemap sources, in order. */
    public static int[] getLabelIds() {
        int[] labelIds = new int[SOURCE_OPTIONS.length];
        for (int i = 0; i < labelIds.length; i++) {
            labelIds[i] = SOURCE_OPTIONS[i].labelId;
        }
        return labelIds;
    }
}
