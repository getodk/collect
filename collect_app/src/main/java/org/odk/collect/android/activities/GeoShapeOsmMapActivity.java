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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import org.odk.collect.android.R;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.widgets.GeoShapeWidget;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 */


public class GeoShapeOsmMapActivity extends Activity implements IRegisterReceiver {
    private MapView mapView;
    private ArrayList<Marker> map_markers = new ArrayList<Marker>();
    private PathOverlay pathOverlay;
    public DefaultResourceProxyImpl resource_proxy;
    public int zoom_level = 3;
    public static final int stroke_width = 5;
    public String final_return_string;
    private MapEventsOverlay OverlayEventos;
    private boolean polygon_connection = false;
    private boolean clear_button_test = false;
    private Button clear_button;
    private Button return_button;
    private Button polygon_button;
    private Button layers_button;
    private SharedPreferences sharedPreferences;
    public Boolean layerStatus = false;
    private int selected_layer= -1;
    private ProgressDialog progress;
    public Boolean gpsStatus = true;
    private Button gps_button;
    public MyLocationNewOverlay mMyLocationOverlay;
    public Boolean data_loaded = false;

    private MapHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoshape_osm_layout);
        setTitle(getString(R.string.geoshape_title)); // Setting title of the action
        return_button = (Button) findViewById(R.id.geoshape_Button);
        clear_button = (Button) findViewById(R.id.clear);
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
        mapView = (MapView)findViewById(R.id.geoshape_mapview);
        mHelper = new MapHelper(this,mapView,GeoShapeOsmMapActivity.this);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMapListener(mapViewListner);
        overlayPointPathListner();
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map_markers.size() != 0){
                    showClearDialog();
//                    clearFeatures();
//                    if (polygon_connection){
//                        //clearFeatures();
//                        showClearDialog();
//                    }else{
//                        Marker c_mark = map_markers.get(map_markers.size()-1);
//                        mapView.getOverlays().remove(c_mark);
//                        map_markers.remove(map_markers.size()-1);
//                        update_polygon();
//                        mapView.invalidate();
//                    }
                }
            }
        });
        layers_button = (Button)findViewById(R.id.layers);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog();

            }
        });

        gps_button = (Button)findViewById(R.id.gps);
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                zoomToMyLocation();
                //setGPSStatus();
            }
        });


        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(centerAroundFix);

        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.getting_location));
        progress.setMessage(getString(R.string.please_wait_long));


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
                    GeoPoint point = new GeoPoint(34.08145, -39.85007);
                    mapView.getController().setZoom(3);
                    mapView.getController().setCenter(point);
                }
            }, 100);
            upMyLocationOverlayLayers();
//            setGPSStatus();
            progress.show();

        }

        mapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHelper.setBasemap();
        upMyLocationOverlayLayers();
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


    private void overlayIntentPolygon(String str){
        clear_button.setVisibility(View.VISIBLE);
        clear_button_test = true;
        String s = str.replace("; ",";");
        String[] sa = s.split(";");
        for (int i=0;i<(sa.length);i++){
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
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
        upMyLocationOverlayLayers();
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
        alertDialogBuilder.setMessage(getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_gps),
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                // Intent callGPSSettingIntent = new Intent(
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                                //startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
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
            mapView.getController().setZoom(15);
            mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
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
        clear_button.setVisibility(View.GONE);
        overlayPointPathListner();
        overlayMyLocationLayers();
        mapView.invalidate();

    }

    private void showClearDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.geo_clear_warning))
                .setPositiveButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearFeatures();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();

    }
    private void showPolyonErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.polygon_validator))
                .setPositiveButton(getString(R.string.dialog_continue), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

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
    private void buildPolygon(){
        if (polygon_connection){
            showClearDialog();
        }else{
            if (map_markers.size()>2){
                //map_markers.add(map_markers.get(0));
                pathOverlay.addPoint(map_markers.get(0).getPosition());
                mapView.invalidate();
                mapView.getOverlays().remove(OverlayEventos);
            }else{
                showPolyonErrorDialog();
            }
        }

    }
    private void update_polygon(){
        pathOverlay.clearPath();
        for (int i =0;i<map_markers.size();i++){
            pathOverlay.addPoint(map_markers.get(i).getPosition());
        }
        pathOverlay.addPoint(map_markers.get(0).getPosition());
        mapView.invalidate();
    }

    private MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean longPressHelper(GeoPoint point) {
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
            update_polygon();
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


    private OnMarkerClickListener nullmarkerlistner= new Marker.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    /*
        This functions should be added to the mapHelper Class

     */
    private void zoomToCentroid(){
        mapView.getController().setZoom(15);
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
