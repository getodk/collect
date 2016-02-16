package org.odk.collect.android.spatial;

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
import org.odk.collect.android.preferences.PreferencesActivity;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MapHelper {
    public static Context context;
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
    private static final String MAPQUEST_MAP_STREETS = "mapquest_streets";
    private static final String MAPQUEST_MAP_SATELLITE = "mapquest_satellite";
    private int selected_layer = 0;

    public static String[] geofileTypes = new String[] {".mbtiles",".kml",".kmz"};
    private final static String slash = File.separator;

    private TilesOverlay osmTileOverlay;
    private TileOverlay googleTileOverlay;
    private IRegisterReceiver iRegisterReceiver;


    public MapHelper(Context pContext,GoogleMap pGoogleMap){
        context = pContext;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        offilineOverlays = getOfflineLayerList();
        this.mGoogleMap = pGoogleMap;

    }

    public MapHelper(Context pContext,MapView pOsmMap,IRegisterReceiver pIregisterReceiver){
        context = pContext;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        offilineOverlays = getOfflineLayerList();
        iRegisterReceiver = pIregisterReceiver;
        this.mOsmMap = pOsmMap;


    }

    private static String _getGoogleBasemap(){
        return sharedPreferences.getString(PreferencesActivity.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
    }
    private static String _getOsmBasemap(){
        return sharedPreferences.getString(PreferencesActivity.KEY_MAP_BASEMAP, MAPQUEST_MAP_STREETS);
    }
    public void setBasemap(){
        if(mGoogleMap != null){
            String basemap = _getGoogleBasemap();
            if (basemap.equals(GOOGLE_MAP_STREETS)) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }else if (basemap.equals(GOOGLE_MAP_SATELLITE)){
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }else if(basemap.equals(GOOGLE_MAP_TERRAIN)){
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }else if(basemap.equals(GOOGLE_MAP_HYBRID)){
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }else{
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }else{
            //OSMMAP
            String basemap = _getOsmBasemap();
            if (basemap.equals(MAPQUEST_MAP_STREETS)) {
                mOsmMap.setTileSource(TileSourceFactory.MAPQUESTOSM);
            }else if(basemap.equals(MAPQUEST_MAP_SATELLITE)){
                mOsmMap.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
            }else{
                mOsmMap.setTileSource(TileSourceFactory.MAPQUESTOSM);
            }

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
        AlertDialog.Builder builder = layerDialod.setSingleChoiceItems(offilineOverlays, selected_layer, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        if (mGoogleMap != null) {
                            if(googleTileOverlay != null){
                                googleTileOverlay.remove();
                            }

                        }else{
                            //OSM
                            if(osmTileOverlay != null){
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
                                    if(googleTileOverlay != null){
                                        googleTileOverlay.remove();
                                    }
                                    TileOverlayOptions opts = new TileOverlayOptions();
                                    GoogleMapsMapBoxOfflineTileProvider provider = new GoogleMapsMapBoxOfflineTileProvider(spfile);
                                    opts.tileProvider(provider);
                                    googleTileOverlay = mGoogleMap.addTileOverlay(opts);
                                } catch (Exception e) {
                                    break;
                                }
                            } else {
                                if(osmTileOverlay != null){
                                    mOsmMap.getOverlays().remove(osmTileOverlay);
                                    mOsmMap.invalidate();
                                }
                                mOsmMap.invalidate();
                                OsmMBTileProvider mbprovider = new OsmMBTileProvider(iRegisterReceiver, spfile);
                                osmTileOverlay = new TilesOverlay(mbprovider,context);
                                osmTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                                mOsmMap.getOverlays().add(0,osmTileOverlay);
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

    private File[] getFileFromSelectedItem(int item){
        File directory = new File(Collect.OFFLINE_LAYERS+slash+offilineOverlays[item]);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.toLowerCase().endsWith(".mbtiles"));
            }
        });
        return files;
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
