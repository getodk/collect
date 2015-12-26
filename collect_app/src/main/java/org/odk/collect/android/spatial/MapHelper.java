package org.odk.collect.android.spatial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;

public class MapHelper {
    public static Context context;
    private static SharedPreferences sharedPreferences;
    public static String[] offilineOverlays;
    private static final String no_folder_key = "None";

    // GOOGLE MAPS BASEMAPS
    private static final String GOOGLE_MAP_STREETS = "streets";
    private static final String GOOGLE_MAP_SATELLITE = "satellite";
    private static final String GOOGLE_MAP_TERRAIN = "terrain‎";
    private static final String GOOGLE_MAP_HYBRID = "hybrid";

    //OSM MAP BASEMAPS
    private static final String MAPQUEST_MAP_STREETS = "mapquest_streets";
    private static final String MAPQUEST_MAP_SATELLITE = "mapquest_satellite";
    private int selected_layer = 0;


    public MapHelper(Context pContext){
        context = pContext;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        offilineOverlays = getOfflineLayerList();

    }

//    public String[] getOffilineOverlays() {
//        return offilineOverlays;
//    }

    private static String _getGoogleBasemap(){
        return sharedPreferences.getString(PreferencesActivity.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
    }
    private static String _getOsmBasemap(){
        return sharedPreferences.getString(PreferencesActivity.KEY_MAP_BASEMAP, MAPQUEST_MAP_STREETS);
    }
    public void setGoogleBasemap(GoogleMap mMap){
        String basemap = _getGoogleBasemap();
        if (basemap.equals(GOOGLE_MAP_STREETS)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }else if (basemap.equals(GOOGLE_MAP_SATELLITE)){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else if(basemap.equals(GOOGLE_MAP_TERRAIN)){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }else if(basemap.equals(GOOGLE_MAP_HYBRID)){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void setOsmBasemap(MapView mMap){
        String basemap = _getOsmBasemap();
        if (basemap.equals(MAPQUEST_MAP_STREETS)) {
            mMap.setTileSource(TileSourceFactory.MAPQUESTOSM);
        }else if(basemap.equals(MAPQUEST_MAP_SATELLITE)){
            mMap.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
        }else{
            mMap.setTileSource(TileSourceFactory.MAPQUESTOSM);
        }

    }

    public static String[] getOfflineLayerList() {
        // TODO Auto-generated method stub
        File files = new File(Collect.OFFLINE_LAYERS);
        ArrayList<String> results = new ArrayList<String>();
        results.add(no_folder_key);
        String[] overlay_folders =  files.list();
        for(int i =0;i<overlay_folders.length;i++){
            results.add(overlay_folders[i]);
        }
        String[] finala = new String[results.size()];
        finala = results.toArray(finala);
        return finala;
    }
    public void showLayersDialog(){
        AlertDialog.Builder layerDialod = new AlertDialog.Builder(context);
        layerDialod.setTitle(context.getString(R.string.select_offline_layer));
        layerDialod.setSingleChoiceItems(offilineOverlays,selected_layer,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                selected_layer = item;
                dialog.dismiss();
            }
        });
        layerDialod.show();
    }

//    public static String getMBTileFromItem(final int item) {
//        String folderName = OffilineOverlays[item];
//        File dir = new File(Collect.OFFLINE_LAYERS+File.separator+folderName);
//        if (dir.isFile()) {
//            // we already have a file
//            return dir.getAbsolutePath();
//        }
//        // search first mbtiles file in the directory
//        String mbtilePath;
//        final File[] files = dir.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(final File dir, final String name) {
//                return name.toLowerCase().endsWith(".mbtiles");
//            }
//        });
//        if (files.length == 0) {
//            throw new RuntimeException(Collect.getInstance().getString(R.string.mbtiles_not_found, dir.getAbsolutePath()));
//        }
//        mbtilePath = files[0].getAbsolutePath();
//        return mbtilePath;
//    }

}
