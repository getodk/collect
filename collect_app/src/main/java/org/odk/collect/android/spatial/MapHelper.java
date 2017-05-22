/*
 * Copyright (C) 2015 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.spatial;

/**
 * Created by jnordling on 12/29/15.
 *
 * @author jonnordling@gmail.com
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.common.collect.ObjectArrays;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

public class MapHelper {
    private static String[] allAvailableOverlays;

    private GoogleMap googleMap;
    private MapView osmMap;

    // GOOGLE MAPS BASEMAPS
    private static final String GOOGLE_MAP_STREETS = "streets";
    private static final String GOOGLE_MAP_SATELLITE = "satellite";
    private static final String GOOGLE_MAP_TERRAIN = "terrain‎";
    private static final String GOOGLE_MAP_HYBRID = "hybrid";

    //OSM MAP BASEMAPS
    private static final String OPENMAP_STREETS = "openmap_streets";
    private static final String OPENMAP_USGS_TOPO = "openmap_usgs_topo";
    private static final String OPENMAP_USGS_SAT = "openmap_usgs_sat";
    private static final String OPENMAP_STAMEN_TERRAIN = "openmap_stamen_terrain";
    private static final String OPENMAP_CARTODB_POSITRON = "openmap_cartodb_positron";
    private static final String OPENMAP_CARTODB_DARKMATTER = "openmap_cartodb_darkmatter";
    private int selectedLayer = 0;

    public static String[] geofileTypes = new String[]{".mbtiles", ".kml", ".kmz"};
    private static final String slash = File.separator;
    private String basemap;

    private TilesOverlay osmTileOverlay;
    private TileOverlay googleTileOverlay;
    private IRegisterReceiver iregisterReceiver;

    private org.odk.collect.android.spatial.TileSourceFactory tileFactory;


    public MapHelper(Context context, GoogleMap googleMap, String basemap) {
        this.googleMap = null;
        osmMap = null;
        this.googleMap = googleMap;
        allAvailableOverlays = ObjectArrays.concat(getOnlineLayerList(context), getOfflineLayerList(), String.class);
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(context);
        this.basemap = basemap;
    }

    public MapHelper(Context context, MapView osmMap, IRegisterReceiver iregisterReceiver, String basemap) {
        googleMap = null;
        this.osmMap = null;
        allAvailableOverlays = ObjectArrays.concat(getOnlineLayerList(context), getOfflineLayerList(), String.class);
        this.iregisterReceiver = iregisterReceiver;
        this.osmMap = osmMap;
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(context);
        this.basemap = basemap;
    }

    public static String getGoogleBasemap(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
    }

    public static String getOsmBasemap(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_MAP_BASEMAP, OPENMAP_STREETS);
    }

    public void setBasemap() {
        if (googleMap != null) {
            switch (basemap) {
                case GOOGLE_MAP_STREETS:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case GOOGLE_MAP_SATELLITE:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case GOOGLE_MAP_TERRAIN:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case GOOGLE_MAP_HYBRID:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                default:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
            }
        } else {
            //OSMMAP

            ITileSource tileSource;

            switch (basemap) {
                case OPENMAP_USGS_TOPO:
                    tileSource = tileFactory.getUSGSTopo();
                    break;

                case OPENMAP_USGS_SAT:
                    tileSource = tileFactory.getUsgsSat();
                    break;

                case OPENMAP_STAMEN_TERRAIN:
                    tileSource = tileFactory.getStamenTerrain();
                    break;

                case OPENMAP_CARTODB_POSITRON:
                    tileSource = tileFactory.getCartoDbPositron();
                    break;

                case OPENMAP_CARTODB_DARKMATTER:
                    tileSource = tileFactory.getCartoDbDarkMatter();
                    break;

                case OPENMAP_STREETS:
                default:
                    tileSource = TileSourceFactory.MAPNIK;
                    break;
            }

            if (tileSource != null) {
                osmMap.setTileSource(tileSource);
            }
        }

    }

    private static String[] getOfflineLayerList() {
        File[] files = new File(Collect.OFFLINE_LAYERS).listFiles();
        ArrayList<String> results = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                results.add(f.getName());
            }
        }

        return results.toArray(new String[0]);
    }

    private String[] getOnlineLayerList(Context context) {
        if (googleMap != null) {
            return context.getResources()
                    .getStringArray(R.array.map_google_basemap_selector_entries);
        } else {
            return context.getResources()
                    .getStringArray(R.array.map_osm_basemap_selector_entries);
        }
    }

    public void showLayersDialog(final Context context) {
        AlertDialog.Builder layerDialog = new AlertDialog.Builder(context);
        layerDialog.setTitle(context.getString(R.string.select_offline_layer));
        layerDialog.setSingleChoiceItems(allAvailableOverlays,
                selectedLayer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (isOfflineOptionSelected(item)) {
                            File[] spFiles = getFileFromSelectedItem(item);
                            File spfile = spFiles[0];

                            if (googleMap != null) {
                                try {
                                    //googleMap.clear();
                                    if (googleTileOverlay != null) {
                                        googleTileOverlay.remove();
                                    }
                                    TileOverlayOptions opts = new TileOverlayOptions();
                                    GoogleMapsMapBoxOfflineTileProvider provider =
                                            new GoogleMapsMapBoxOfflineTileProvider(spfile);
                                    opts.tileProvider(provider);
                                    googleTileOverlay = googleMap.addTileOverlay(opts);
                                } catch (Exception e) {
                                    Timber.e(e);
                                }
                            } else {
                                if (osmTileOverlay != null) {
                                    osmMap.getOverlays().remove(osmTileOverlay);
                                    osmMap.invalidate();
                                }
                                osmMap.invalidate();
                                OsmMBTileProvider mbprovider = new OsmMBTileProvider(
                                        iregisterReceiver, spfile);
                                osmTileOverlay = new TilesOverlay(mbprovider, context);
                                osmTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                                osmMap.getOverlays().add(0, osmTileOverlay);
                                osmMap.invalidate();
                            }
                        } else {
                            if (googleMap != null) {
                                if (googleTileOverlay != null) {
                                    googleTileOverlay.remove();
                                }
                                basemap = context
                                        .getResources()
                                        .getStringArray(R.array.map_google_basemap_selector_entry_values)[item];

                            } else {
                                //OSM
                                if (osmTileOverlay != null) {
                                    osmMap.getOverlays().remove(osmTileOverlay);
                                    osmMap.invalidate();
                                }
                                basemap = context
                                        .getResources()
                                        .getStringArray(R.array.map_osm_basemap_selector_entry_values)[item];
                            }
                            setBasemap();
                        }
                        selectedLayer = item;
                        dialog.dismiss();
                    }
                });
        layerDialog.show();
    }

    private boolean isOfflineOptionSelected(int item) {
        return getFileFromSelectedItem(item).length > 0;
    }

    private File[] getFileFromSelectedItem(int item) {
        File[] files = new File[0];
        File directory = new File(Collect.OFFLINE_LAYERS + slash + allAvailableOverlays[item]);
        if (directory.isDirectory()) {
            files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return (filename.toLowerCase(Locale.US).endsWith(".mbtiles"));
                }
            });
        }
        return files;
    }
}
