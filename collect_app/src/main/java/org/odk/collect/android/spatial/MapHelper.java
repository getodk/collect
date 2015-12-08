package org.odk.collect.android.spatial;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import android.util.Log;

public class MapHelper {
    public static String[] OffilineOverlays = getOfflineLayerList();
    public final static  String MAPBOX = "mapbox";
    public final static  String GOOGLE = "google";
    public final static String ESRI = "esri";

    public String[] getOffilineOverlays() {
        return OffilineOverlays;
    }



    public static ITileSource getTileSource(String basemap) {
		ITileSource baseTiles;
        String[] baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.0d981b0d/"};
        String mapProvider;
        if (basemap.equals("Default")){
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.0d981b0d/"
            };
        }else if(basemap.equals("Steets Classic")){
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.n141ednk/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.n141ednk/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.n141ednk/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.n141ednk/"
            };
        }else if(basemap.equals("Outdoors")){
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.n1417e4k/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.n1417e4k/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.n1417e4k/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.n1417e4k/"
            };
        }else if(basemap.equals("Dark")){
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.n1425ld2/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.n1425ld2/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.n1425ld2/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.n1425ld2/"
            };
        }else if(basemap.equals("Activities")) {
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
            };
        }else if (basemap.equals("Google Maps Street")){
            mapProvider = GOOGLE;
            baseURL= new String[]{
                    "http://mt0.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
                    "http://mt1.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
                    "http://mt2.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
                    "http://mt3.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d" };
        }else if (basemap.equals("Google Maps Satellite")){
            mapProvider = GOOGLE;
            baseURL= new String[]{
                    "http://khms0.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
                    "http://khms1.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
                    "http://khms2.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
                    "http://khms3.googleapis.com/kh?v=186&x=%d&y=%d&z=%d" };
        }else{
            //Else nothing
            mapProvider = MAPBOX;
            baseURL = new String[]{
                    "http://a.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://b.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://c.tiles.mapbox.com/v4/jonnordling.0d981b0d/",
                    "http://d.tiles.mapbox.com/v4/jonnordling.0d981b0d/"
            };
        	
        }
        if (mapProvider == MAPBOX) {
            final String accessToken = "pk.eyJ1Ijoiam9ubm9yZGxpbmciLCJhIjoiZTcwNDcxN2ZiMWU0YTZhZjM2ZWFlNTMxZWI4Y2QwNWMifQ.mMQKvbPR2IYIv7DsV2HU4A#4";
            baseTiles = new XYTileSource(basemap, null, 1, 22, 256, ".png", baseURL){
                @Override
                public String getTileURLString(MapTile aTile) {
                    String str = super.getTileURLString(aTile) + "?access_token=" + accessToken;
                    return str;
                }
            };
        }else{
            baseTiles = new GoogleEarthMarkTileSource(basemap,baseURL);
        }
        return baseTiles;
	}
    public static String[] getOfflineLayerList() {
        // TODO Auto-generated method stub
        File files = new File(Collect.OFFLINE_LAYERS);
        ArrayList<String> results = new ArrayList<String>();
        results.add("None");
        String[] overlay_folders =  files.list();
        for(int i =0;i<overlay_folders.length;i++){
            results.add(overlay_folders[i]);
        }
        String[] finala = new String[results.size()];
        finala = results.toArray(finala);
        return finala;
    }

    public static String getMBTileFromItem(final int item) {
        String folderName = OffilineOverlays[item];
        File dir = new File(Collect.OFFLINE_LAYERS+File.separator+folderName);

        if (dir.isFile()) {
            // we already have a file
            return dir.getAbsolutePath();
        }

        // search first mbtiles file in the directory
        String mbtilePath;
        final File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".mbtiles");
            }
        });

        if (files.length == 0) {
            throw new RuntimeException(Collect.getInstance().getString(R.string.mbtiles_not_found, dir.getAbsolutePath()));
        }
        mbtilePath = files[0].getAbsolutePath();

        return mbtilePath;
    }

}
