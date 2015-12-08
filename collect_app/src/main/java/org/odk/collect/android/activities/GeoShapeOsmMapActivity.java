/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.activities;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.osmdroid.tileprovider.IRegisterReceiver;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 *
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.MapSettings;
import org.odk.collect.android.spatial.MBTileProvider;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.widgets.GeoShapeWidget;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;


public class GeoShapeOsmMapActivity extends Activity implements IRegisterReceiver {
    private MapView mapView;
    private ArrayList<Marker> map_markers = new ArrayList<>();
    private PathOverlay pathOverlay;
    private ITileSource baseTiles;
    public DefaultResourceProxyImpl resource_proxy;
    public int zoom_level = 3;
    public static final int stroke_width = 5;
    public String final_return_string;
    private MapEventsOverlay OverlayEventos;
    private boolean polygon_connection = false;
    private boolean clear_button_test = false;
    private ImageButton clear_button;
    private ImageButton return_button;
    private ImageButton polygon_button;
    private SharedPreferences sharedPreferences;
    public Boolean layerStatus = false;
    private int selected_layer= -1;
    private ProgressDialog progress;

    private MBTileProvider mbprovider;
    private TilesOverlay mbTileOverlay;
    public Boolean gpsStatus = true;
    private ImageButton gps_button;
    private String[] OffilineOverlays;
    public MyLocationNewOverlay mMyLocationOverlay;
    public Boolean data_loaded = false;


    @Override
    protected void onResume() {
        super.onResume();
        Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
        baseTiles = MapHelper.getTileSource(basemap);
        mapView.setTileSource(baseTiles);
        mapView.setUseDataConnection(online);
        setGPSStatus();
    }

    @Override
    public void onBackPressed() {
        saveGeoShape();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableMyLocation();
    }
    @Override
    protected void onStop() {
        super.onStop();
        disableMyLocation();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/*
			Setting Content  & initiating the main button id
			for the activity
		  */
        setContentView(R.layout.geoshape_layout);
        setTitle(getString(R.string.geoshape_title)); // Setting title of the action
        return_button = (ImageButton) findViewById(R.id.geoshape_Button);
        polygon_button = (ImageButton) findViewById(R.id.polygon_button);
        clear_button = (ImageButton) findViewById(R.id.clear_button);

		/*
			Defining the System prefereces from the mapSetting
		  */

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
        baseTiles = MapHelper.getTileSource(basemap);

        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
        mapView = (MapView)findViewById(R.id.geoshape_mapview);
        mapView.setTileSource(baseTiles);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setUseDataConnection(online);
        mapView.setMapListener(mapViewListner);

        overlayPointPathListner();


        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });
        polygon_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildPolygon();
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map_markers.size() != 0){
                    if (polygon_connection){
                        //clearFeatures();
                        showClearDialog();
                    }else{
                        Marker c_mark = map_markers.get(map_markers.size()-1);
                        mapView.getOverlays().remove(c_mark);
                        map_markers.remove(map_markers.size()-1);
                        update_polygon();
                        mapView.invalidate();
                    }
                }
            }
        });
        ImageButton layers_button = (ImageButton)findViewById(R.id.geoShape_layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showLayersDialog();

            }
        });

        gps_button = (ImageButton)findViewById(R.id.geoshape_gps_button);
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setGPSStatus();
            }
        });


        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(centerAroundFix);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading Location");
        progress.setMessage("Wait while loading...");


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if ( intent.hasExtra(GeoShapeWidget.SHAPE_LOCATION) ) {
                data_loaded =true;
                String s = intent.getStringExtra(GeoShapeWidget.SHAPE_LOCATION);
                overlayIntentPolygon(s);
                zoomToCentroid();
            }
        }else{

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //Do something after 100ms
                    GeoPoint  point = new GeoPoint(34.08145, -39.85007);
                    mapView.getController().setZoom(3);
                    mapView.getController().setCenter(point);
                }
            }, 100);

            setGPSStatus();
            progress.show();

        }

        mapView.invalidate();
    }

    private void buildPolygon(){
        if (polygon_connection){
            showClearDialog();
        }else{
            if (map_markers.size()>2){
                map_markers.add(map_markers.get(0));
                pathOverlay.addPoint(map_markers.get(0).getPosition());
                mapView.invalidate();
                polygon_connection= true;
                polygon_button.setVisibility(View.GONE);
                mapView.getOverlays().remove(OverlayEventos);
            }else{
                showPolyonErrorDialog();
            }
        }

    }
    private void overlayIntentPolygon(String str){
        clear_button.setVisibility(View.VISIBLE);
        clear_button_test = true;
        String s = str.replace("; ",";");
        String[] sa = s.split(";");
        for (int i=0;i<(sa.length -1);i++){
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
//			gp[0] = Double.valueOf(lat).doubleValue();
//			gp[1] = Double.valueOf(lng).doubleValue();
            Marker marker = new Marker(mapView);
            GeoPoint point = new GeoPoint(gp[0], gp[1]);
            marker.setPosition(point);
            marker.setDraggable(true);
            marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener(nullmarkerlistner);
            map_markers.add(marker);
            pathOverlay.addPoint(marker.getPosition());
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(draglistner);
            mapView.getOverlays().add(marker);
        }
        buildPolygon();
        mapView.getOverlays().remove(OverlayEventos);
    }

    private void setGPSStatus(){
        if(gpsStatus ==false){
            gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
            upMyLocationOverlayLayers();
            //enableMyLocation();
            //zoomToMyLocation();
            gpsStatus = true;
        }else{
            gps_button.setImageResource(R.drawable.ic_menu_mylocation);
            disableMyLocation();
            gpsStatus = false;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable centerAroundFix = new Runnable() {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    zoomToMyLocation();
                    progress.dismiss();
                }
            });
        }
    };

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                // Intent callGPSSettingIntent = new Intent(
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                                //startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void upMyLocationOverlayLayers(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            overlayMyLocationLayers();
            //zoomToMyLocation();
        }else{
            showGPSDisabledAlertToUser();
        }

    }

    private void overlayMyLocationLayers(){
        mapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
    }
    private void zoomToMyLocation(){
        if (mMyLocationOverlay.getMyLocation()!= null){
            if (zoom_level ==3){
                mapView.getController().setZoom(15);
            }else{
                mapView.getController().setZoom(zoom_level);
            }
            mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
            //mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
        }else{
            mapView.getController().setZoom(zoom_level);
        }

    }
    private void disableMyLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            mMyLocationOverlay.setEnabled(false);
            mMyLocationOverlay.disableFollowLocation();
            mMyLocationOverlay.disableMyLocation();
            gpsStatus =false;
        }
    }



    private void saveGeoShape(){
        //Toast.makeText(this, "Do Save Stuff", Toast.LENGTH_LONG).show();
        returnLocation();
        finish();
    }

    private void overlayPointPathListner(){
        OverlayEventos = new MapEventsOverlay(getBaseContext(), mReceive);
        pathOverlay= new PathOverlay(Color.RED, this);
        Paint pPaint = pathOverlay.getPaint();
        pPaint.setStrokeWidth(stroke_width);
        mapView.getOverlays().add(pathOverlay);
        mapView.getOverlays().add(OverlayEventos);
        mapView.invalidate();
    }
    private void clearFeatures(){
        polygon_connection = false;
        clear_button_test = false;
        map_markers.clear();
        pathOverlay.clearPath();
        mapView.getOverlays().clear();
        //clearMarkersOverlay();
        polygon_button.setVisibility(View.VISIBLE);
        clear_button.setVisibility(View.GONE);
        if(gpsStatus){
            upMyLocationOverlayLayers();

        }
        overlayPointPathListner();

        mapView.invalidate();

    }


    private void showClearDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Polygon already created. Would you like to CLEAR the feature?")
                .setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        clearFeatures();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                }).show();

    }
    private void showPolyonErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Must have at least 3 points to create Polygon")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                }).show();

    }
    private String generateReturnString() {
        String temp_string = "";
        for (int i = 0 ; i < map_markers.size();i++){
            String lat = Double.toString(map_markers.get(i).getPosition().getLatitude());
            String lng = Double.toString(map_markers.get(i).getPosition().getLongitude());
            String alt ="0.0";
            String acu = "0.0";
            temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
        }
        return temp_string;
    }

    private void returnLocation(){
        final_return_string = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                final_return_string);
        setResult(RESULT_OK, i);
        finish();
    }


    private void update_polygon(){
        pathOverlay.clearPath();
        for (int i =0;i<map_markers.size();i++){
            pathOverlay.addPoint(map_markers.get(i).getPosition());
        }
        mapView.invalidate();
    }
    private MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean longPressHelper(GeoPoint point) {
            //Toast.makeText(GeoShapeActivity.this, point.getLatitude()+" ", Toast.LENGTH_LONG).show();
            //map_points.add(point);
            if (!clear_button_test){
                clear_button.setVisibility(View.VISIBLE);
                clear_button_test = true;
            }
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setDraggable(true);
            marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener(nullmarkerlistner);
            map_markers.add(marker);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(draglistner);
            mapView.getOverlays().add(marker);
            pathOverlay.addPoint(marker.getPosition());
            mapView.invalidate();
            return false;
        }

        @Override
        public boolean singleTapConfirmedHelper(GeoPoint arg0) {
            return false;
        }
    };

    private MapListener mapViewListner = new MapListener() {
        @Override
        public boolean onZoom(ZoomEvent zoomLev) {
            zoom_level = zoomLev.getZoomLevel();
            return false;
        }
        @Override
        public boolean onScroll(ScrollEvent arg0) {
            return false;
        }

    };

    private OnMarkerDragListener draglistner = new OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }
        @Override
        public void onMarkerDragEnd(Marker marker) {
            update_polygon();

        }
        @Override
        public void onMarkerDrag(Marker marker) {
            update_polygon();

        }
    };

    private void showLayersDialog() {
        //FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
        //View view=fl.inflate(self, R.layout.showlayers_layout, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Select Offline Layer");
        OffilineOverlays = getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
        //alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
        alertDialog.setSingleChoiceItems(OffilineOverlays,selected_layer,new  DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                //Toast.makeText(OSM_Map.this,item, Toast.LENGTH_LONG).show();
                // The 'which' argument contains the index position
                // of the selected item
                //Toast.makeText(OSM_Map.this,item +" ", Toast.LENGTH_LONG).show();

                switch(item){
                    case 0 :
                        mapView.getOverlays().remove(mbTileOverlay);
                        layerStatus =false;
                        // Reset max zoom level to max level of baseMap tile layer
                        int baseMapMaxZoomLevel = baseTiles.getMaximumZoomLevel();
                        mapView.setMaxZoomLevel(baseMapMaxZoomLevel);
                        break;
                    default:
                        layerStatus = true;
                        mapView.getOverlays().remove(mbTileOverlay);
                        //String mbTileLocation = getMBTileFromItem(item);
                        String mbFilePath = getMBTileFromItem(item);
                        //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
                        File mbFile = new File(mbFilePath);
                        mbprovider = new MBTileProvider(GeoShapeOsmMapActivity.this, mbFile);
                        int newMaxZoomLevel = mbprovider.getMaximumZoomLevel();
                        mbTileOverlay = new TilesOverlay(mbprovider,GeoShapeOsmMapActivity.this);
                        mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                        mapView.getOverlays().add(mbTileOverlay);
                        updateMapOverLayOrder();
                        mapView.setMaxZoomLevel(newMaxZoomLevel);
                        mapView.invalidate();
                }
                //This resets the map and sets the selected Layer
                selected_layer =item;
                dialog.dismiss();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapView.invalidate();
                    }
                }, 400);

            }
        });
        //alertDialog.setView(view);
        alertDialog.show();

    }

    private void updateMapOverLayOrder(){
        List<Overlay> overlays = mapView.getOverlays();
        if (layerStatus){
            mapView.getOverlays().remove(mbTileOverlay);
            mapView.getOverlays().remove(pathOverlay);
            mapView.getOverlays().add(mbTileOverlay);
            mapView.getOverlays().add(pathOverlay);

        }
        for (Overlay overlay : overlays){
            //Class x = overlay.getClass();
            final Overlay o = overlay;
            if (overlay.getClass() == Marker.class){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mapView.getOverlays().remove(o);
                        mapView.invalidate();
                    }
                }, 100);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mapView.getOverlays().add(o);
                        mapView.invalidate();
                    }
                }, 100);
                //mapView.getOverlays().remove(overlay);
                //mapView.getOverlays().add(overlay);

            }
        }
        mapView.invalidate();

    }

    private String getMBTileFromItem(int item) {
        String foldername = OffilineOverlays[item];
        File dir = new File(Collect.OFFLINE_LAYERS+File.separator+foldername);
        String mbtilePath;
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mbtiles");
            }
        });
        mbtilePath =Collect.OFFLINE_LAYERS+File.separator+foldername+File.separator+files[0].getName();
        //returnFile = new File(Collect.OFFLINE_LAYERS+File.separator+foldername+files[0]);

        return mbtilePath;
    }
    private String[] getOfflineLayerList() {
        File files = new File(Collect.OFFLINE_LAYERS);
        ArrayList<String> results = new ArrayList<>();
        results.add("None");
//		 String[] overlay_folders =  files.list();
        for(String folder : files.list()){
            results.add(folder);
        }
//		 for(int i =0;i<overlay_folders.length;i++){
//			 results.add(overlay_folders[i]);
//			 //Toast.makeText(self, overlay_folders[i]+" ", Toast.LENGTH_LONG).show();
//		 }
        String[] finala = new String[results.size()];
        finala = results.toArray(finala);
		 /*for(int j = 0;j<finala.length;j++){
			 Toast.makeText(self, finala[j]+" ", Toast.LENGTH_LONG).show();
		 }*/
        return finala;
    }

    private OnMarkerClickListener nullmarkerlistner= new Marker.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    private void zoomToCentroid(){

			/*
				Calculate Centroid of Polygon
			 */

        //----- This should be hard coded but based on the extent of the points
        mapView.getController().setZoom(15);
        //-----

        mapView.invalidate();
        Handler handler=new Handler();
        Runnable r = new Runnable(){
            public void run() {
                Integer size  = map_markers.size();
                Double x_value = 0.0;
                Double y_value = 0.0;
                for(int i=0; i<size; i++){
                    GeoPoint temp_marker = map_markers.get(i).getPosition();
                    Double x_marker = temp_marker.getLatitude();
                    Double y_marker = temp_marker.getLongitude();
                    x_value += x_marker;
                    y_value += y_marker;
                }
                Double x_cord = x_value/size;
                Double y_cord = y_value/size;
                GeoPoint centroid = new GeoPoint(x_cord,y_cord);
                mapView.getController().setCenter(centroid);
            }
        };
        handler.post(r);
        mapView.invalidate();

    }


}
