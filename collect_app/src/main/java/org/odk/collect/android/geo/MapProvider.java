package org.odk.collect.android.geo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.geo.GoogleMapConfigurator.GoogleMapTypeOption;
import org.odk.collect.android.geo.MapboxMapConfigurator.MapboxUrlOption;
import org.odk.collect.android.geo.OsmDroidMapConfigurator.WmsOption;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.shared.Settings;

import java.util.Map;
import java.util.WeakHashMap;

import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_STAMEN;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_USGS_MAP_STYLE;

/**
 * Obtains a MapFragment according to the user's preferences.
 * This is the top-level class that should be used by the rest of the application.
 * The available options on the Maps preferences screen are also defined here.
 */
public class MapProvider {
    private static final SourceOption[] SOURCE_OPTIONS = initOptions();
    private static final String USGS_URL_BASE =
        "https://basemap.nationalmap.gov/arcgis/rest/services";
    private static final String OSM_COPYRIGHT = "© OpenStreetMap contributors";
    private static final String CARTO_COPYRIGHT = "© CARTO";
    private static final String CARTO_ATTRIBUTION = OSM_COPYRIGHT + ", " + CARTO_COPYRIGHT;
    private static final String STAMEN_ATTRIBUTION = "Map tiles by Stamen Design, under CC BY 3.0.\nData by OpenStreetMap, under ODbL.";
    private static final String USGS_ATTRIBUTION = "Map services and data available from U.S. Geological Survey,\nNational Geospatial Program.";

    // In general, there will only be one MapFragment, and thus one entry, in
    // each of these two Maps at any given time.  Nonetheless, it's a little
    // tidier and less error-prone to use a Map than to track the key and value
    // in separate fields, and the WeakHashMap will conveniently drop the key
    // automatically when it's no longer needed.

    /** Keeps track of the listener associated with a given MapFragment. */
    private final Map<MapFragment, Settings.OnSettingChangeListener>
        listenersByMap = new WeakHashMap<>();

    /** Keeps track of the configurator associated with a given MapFragment. */
    private final Map<MapFragment, MapConfigurator>
        configuratorsByMap = new WeakHashMap<>();

    /**
     * In the preference UI, the available basemaps are organized into "sources"
     * to make them easier to find.  This defines the basemap sources and the
     * basemap options available under each one, in their order of appearance.
     */
    private static SourceOption[] initOptions() {
        return new SourceOption[] {
            new SourceOption(BASEMAP_SOURCE_GOOGLE, R.string.basemap_source_google,
                new GoogleMapConfigurator(
                    KEY_GOOGLE_MAP_STYLE, R.string.basemap_source_google,
                    new GoogleMapTypeOption(GoogleMap.MAP_TYPE_NORMAL, R.string.streets),
                    new GoogleMapTypeOption(GoogleMap.MAP_TYPE_TERRAIN, R.string.terrain),
                    new GoogleMapTypeOption(GoogleMap.MAP_TYPE_HYBRID, R.string.hybrid),
                    new GoogleMapTypeOption(GoogleMap.MAP_TYPE_SATELLITE, R.string.satellite)
                )
            ),
            new SourceOption(BASEMAP_SOURCE_MAPBOX, R.string.basemap_source_mapbox,
                new MapboxMapConfigurator(
                    KEY_MAPBOX_MAP_STYLE, R.string.basemap_source_mapbox,
                    new MapboxUrlOption(Style.MAPBOX_STREETS, R.string.streets),
                    new MapboxUrlOption(Style.LIGHT, R.string.light),
                    new MapboxUrlOption(Style.DARK, R.string.dark),
                    new MapboxUrlOption(Style.SATELLITE, R.string.satellite),
                    new MapboxUrlOption(Style.SATELLITE_STREETS, R.string.hybrid),
                    new MapboxUrlOption(Style.OUTDOORS, R.string.outdoors)
                )
            ),
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
                    KEY_USGS_MAP_STYLE, R.string.basemap_source_usgs,
                    new WmsOption("topographic", R.string.topographic, new WebMapService(
                        R.string.openmap_usgs_topo, 0, 18, 256, USGS_ATTRIBUTION,
                        USGS_URL_BASE + "/USGSTopo/MapServer/tile/{z}/{y}/{x}"
                    )),
                    new WmsOption("hybrid", R.string.hybrid, new WebMapService(
                        R.string.openmap_usgs_sat, 0, 18, 256, USGS_ATTRIBUTION,
                        USGS_URL_BASE + "/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}"
                    )),
                    new WmsOption("satellite", R.string.satellite, new WebMapService(
                        R.string.openmap_usgs_img, 0, 18, 256, USGS_ATTRIBUTION,
                        USGS_URL_BASE + "/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}"
                    ))
                )
            ),
            new SourceOption(BASEMAP_SOURCE_STAMEN, R.string.basemap_source_stamen,
                new OsmDroidMapConfigurator(
                    new WebMapService(
                        R.string.openmap_stamen_terrain, 0, 18, 256, STAMEN_ATTRIBUTION,
                        "http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
                    )
                )
            ),
            new SourceOption(BASEMAP_SOURCE_CARTO, R.string.basemap_source_carto,
                new OsmDroidMapConfigurator(
                    KEY_CARTO_MAP_STYLE, R.string.basemap_source_carto,
                    new WmsOption("positron", R.string.carto_map_style_positron, new WebMapService(
                        R.string.openmap_cartodb_positron, 0, 18, 256, CARTO_ATTRIBUTION,
                        "http://1.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                    )),
                    new WmsOption("dark_matter", R.string.carto_map_style_dark_matter, new WebMapService(
                        R.string.openmap_cartodb_darkmatter, 0, 18, 256, CARTO_ATTRIBUTION,
                        "http://1.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
                    ))
                )
            )
        };
    }

    /** Gets a new MapFragment from the selected MapConfigurator. */
    public MapFragment createMapFragment(Context context) {
        MapConfigurator cftor = getConfigurator();
        MapFragment map = cftor.createMapFragment(context);
        if (map != null) {
            configuratorsByMap.put(map, cftor);
            return map;
        }
        cftor.showUnavailableMessage(context);
        return null;
    }

    /** Gets the currently selected MapConfigurator. */
    public static @NonNull MapConfigurator getConfigurator() {
        return getOption(null).cftor;
    }

    /**
     * Gets the MapConfigurator for the SourceOption with the given id, or the
     * currently selected MapConfigurator if id is null.
     */
    public static @NonNull MapConfigurator getConfigurator(String id) {
        return getOption(id).cftor;
    }

    /** Gets the currently selected SourceOption's label string resource ID. */
    public static int getSourceLabelId() {
        return getOption(null).labelId;
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

    /**
     * Gets the SourceOption with the given id, or the currently selected option
     * if id is null, or the first option if the id is unknown.  Never null.
     */
    private static @NonNull SourceOption getOption(String id) {
        if (id == null) {
            id = PrefUtils.getSharedPrefs().getString(KEY_BASEMAP_SOURCE);
        }
        for (SourceOption option : SOURCE_OPTIONS) {
            if (option.id.equals(id)) {
                return option;
            }
        }
        return SOURCE_OPTIONS[0];
    }

    void onMapFragmentStart(MapFragment map) {
        MapConfigurator cftor = configuratorsByMap.get(map);
        if (cftor != null) {
            Settings generalSettings = PrefUtils.getSharedPrefs();
            Settings.OnSettingChangeListener listener = key -> {
                if (cftor.getPrefKeys().contains(key)) {
                    map.applyConfig(cftor.buildConfig(generalSettings));
                }
            };
            map.applyConfig(cftor.buildConfig(generalSettings));
            generalSettings.registerOnSettingChangeListener(listener);
            listenersByMap.put(map, listener);
        }
    }

    void onMapFragmentStop(MapFragment map) {
        Settings.OnSettingChangeListener listener = listenersByMap.get(map);
        if (listener != null) {
            Settings prefs = PrefUtils.getSharedPrefs();
            prefs.unregisterOnSettingChangeListener(listener);
            listenersByMap.remove(map);
        }
    }

    private static class SourceOption {
        private final String id;  // preference value to store
        private final int labelId;  // string resource ID
        private final MapConfigurator cftor;

        private SourceOption(String id, int labelId, MapConfigurator cftor) {
            this.id = id;
            this.labelId = labelId;
            this.cftor = cftor;
        }
    }
}
