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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

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

public class MapHelper {
    private static SharedPreferences sharedPreferences;
    public static String[] offilineOverlays;
    private static final String no_folder_key = "None";

    public static GoogleMap mGoogleMap;
    public static MapView mOsmMap;

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
    private int selected_layer = 0;

    public static String[] geofileTypes = new String[]{".mbtiles", ".kml", ".kmz"};
    private final static String slash = File.separator;

    private TilesOverlay osmTileOverlay;
    private TileOverlay googleTileOverlay;
    private IRegisterReceiver iRegisterReceiver;

    private org.odk.collect.android.spatial.TileSourceFactory tileFactory;


    public MapHelper(Context pContext, GoogleMap pGoogleMap) {
        this.mGoogleMap = null;
        this.mOsmMap = null;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        offilineOverlays = getOfflineLayerList();
        this.mGoogleMap = pGoogleMap;
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(pContext);
    }

    public MapHelper(Context pContext, MapView pOsmMap, IRegisterReceiver pIregisterReceiver) {
        this.mGoogleMap = null;
        this.mOsmMap = null;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        offilineOverlays = getOfflineLayerList();
        iRegisterReceiver = pIregisterReceiver;
        this.mOsmMap = pOsmMap;
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(pContext);
    }

    private static String _getGoogleBasemap() {
        return sharedPreferences.getString(PreferenceKeys.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
    }

    private static String _getOsmBasemap() {
        return sharedPreferences.getString(PreferenceKeys.KEY_MAP_BASEMAP, OPENMAP_STREETS);
    }

    public void setBasemap() {
        if (mGoogleMap != null) {
            String basemap = _getGoogleBasemap();
            if (basemap.equals(GOOGLE_MAP_STREETS)) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else if (basemap.equals(GOOGLE_MAP_SATELLITE)) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (basemap.equals(GOOGLE_MAP_TERRAIN)) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (basemap.equals(GOOGLE_MAP_HYBRID)) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        } else {
            //OSMMAP
            String basemap = _getOsmBasemap();

            ITileSource tileSource = null;

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
                mOsmMap.setTileSource(tileSource);
            }
        }

    }

    public static String[] getOfflineLayerList() {
        File[] files = new File(Collect.OFFLINE_LAYERS).listFiles();
        ArrayList<String> results = new ArrayList<String>();
        results.add(no_folder_key);
        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                results.add(f.getName());
            }
        }

        return results.toArray(new String[0]);
    }

    public void showLayersDialog(final Context pContext) {
        AlertDialog.Builder layerDialod = new AlertDialog.Builder(pContext);
        layerDialod.setTitle(pContext.getString(R.string.select_offline_layer));
        AlertDialog.Builder builder = layerDialod.setSingleChoiceItems(offilineOverlays,
                selected_layer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                if (mGoogleMap != null) {
                                    if (googleTileOverlay != null) {
                                        googleTileOverlay.remove();
                                    }

                                } else {
                                    //OSM
                                    if (osmTileOverlay != null) {
                                        mOsmMap.getOverlays().remove(osmTileOverlay);
                                        mOsmMap.invalidate();
                                    }
                                }
                                break;
                            default:
                                File[] spFiles = getFileFromSelectedItem(item);
                                if (spFiles.length == 0) {
                                    break;
                                } else {
                                    File spfile = spFiles[0];

                                    if (mGoogleMap != null) {
                                        try {
                                            //mGoogleMap.clear();
                                            if (googleTileOverlay != null) {
                                                googleTileOverlay.remove();
                                            }
                                            TileOverlayOptions opts = new TileOverlayOptions();
                                            GoogleMapsMapBoxOfflineTileProvider provider =
                                                    new GoogleMapsMapBoxOfflineTileProvider(spfile);
                                            opts.tileProvider(provider);
                                            googleTileOverlay = mGoogleMap.addTileOverlay(opts);
                                        } catch (Exception e) {
                                            break;
                                        }
                                    } else {
                                        if (osmTileOverlay != null) {
                                            mOsmMap.getOverlays().remove(osmTileOverlay);
                                            mOsmMap.invalidate();
                                        }
                                        mOsmMap.invalidate();
                                        OsmMBTileProvider mbprovider = new OsmMBTileProvider(
                                                iRegisterReceiver, spfile);
                                        osmTileOverlay = new TilesOverlay(mbprovider, pContext);
                                        osmTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                                        mOsmMap.getOverlays().add(0, osmTileOverlay);
                                        mOsmMap.invalidate();

                                    }
                                    dialog.dismiss();
                                }
                                break;
                        }
                        selected_layer = item;
                        dialog.dismiss();
                    }
                });
        layerDialod.show();

    }

    private File[] getFileFromSelectedItem(int item) {
        File directory = new File(Collect.OFFLINE_LAYERS + slash + offilineOverlays[item]);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.toLowerCase().endsWith(".mbtiles"));
            }
        });
        return files;
    }


}
