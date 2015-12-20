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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.GeoShapeWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Version of the GeoShapeGoogleMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 * @date 12/12/15
 *
 */

public class GeoShapeGoogleMapActivity extends FragmentActivity implements LocationListener, OnMarkerDragListener, OnMapLongClickListener {

    private GoogleMap mMap;
    private UiSettings gmapSettings;
    private LocationManager mLocationManager;
    private Boolean mGPSOn = false;
    private Boolean mNetworkOn =false;
    private Location curLocation;
    private LatLng curlatLng;
    private Boolean initZoom = false;
    private PolygonOptions polygonOptions;
    private Polygon polygon;
    private ArrayList<LatLng> latLngsArray = new ArrayList<LatLng>();
    private ArrayList<Marker> markerArray = new ArrayList<Marker>();
    private ImageButton gps_button;
    private ImageButton clear_button;
    private ImageButton polygon_button;
    private ImageButton return_button;
    private String final_return_string;
    private Boolean data_loaded = false;
    private Boolean clear_button_test;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoshape_google_layout);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);


        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);

        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }


        // If there is a last know location go there
        if(curLocation !=null){
            curlatLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng,16));
            initZoom = true;
        }

        gps_button = (ImageButton)findViewById(R.id.geoshape_gps_button);
        gps_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(curLocation !=null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng,16));
                }
            }
        });
        polygon_button = (ImageButton) findViewById(R.id.polygon_button);
        polygon_button.setVisibility(View.GONE);
        clear_button = (ImageButton) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0){
                    showClearDialog();
                }
            }
        });
        return_button = (ImageButton) findViewById(R.id.geoshape_Button);
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if ( intent.hasExtra(GeoShapeWidget.SHAPE_LOCATION) ) {
                data_loaded =true;
                String s = intent.getStringExtra(GeoShapeWidget.SHAPE_LOCATION);
                overlayIntentPolygon(s);
                zoomToCentroid();
            }
        }

    }

    private void stopGeolocating() {
        // Inititated on pause to stop geoLocations

    }

    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
    	super.onStop();
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

    private void overlayIntentPolygon(String str){
        clear_button.setVisibility(View.VISIBLE);
        clear_button_test = true;
        String s = str.replace("; ",";");
        String[] sa = s.split(";");
        // Set the Marker Array
//        polygonOptions.add(latLng);
//        polygon = mMap.addPolygon(polygonOptions);
        for (int i=0;i<(sa.length);i++){
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            LatLng point = new LatLng(gp[0], gp[1]);
            polygonOptions.add(point);
            MarkerOptions mMarkerOptions = new MarkerOptions().position(point).draggable(true);
            Marker marker= mMap.addMarker(mMarkerOptions);
            markerArray.add(marker);
        }
        polygon = mMap.addPolygon(polygonOptions);
        update_polygon();

    }

    private String generateReturnString() {
        String temp_string = "";
        for (int i = 0 ; i < markerArray.size();i++){
            String lat = Double.toString(markerArray.get(i).getPosition().latitude);
            String lng = Double.toString(markerArray.get(i).getPosition().longitude);
            String alt ="0.0";
            String acu = "0.0";
            temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
        }
        return temp_string;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (data_loaded){
            // turn of the GPS and Polygon button
            polygon_button.setVisibility(View.GONE);

        }
        if (mGPSOn) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        if (mNetworkOn) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        if(!initZoom){
            LatLng mLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng,16));
            initZoom = true;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void update_polygon(){
        ArrayList<LatLng> tempLat =  new ArrayList<LatLng>();
        for (int i =0;i<markerArray.size();i++){
            LatLng latLng = markerArray.get(i).getPosition();
            tempLat.add(latLng);
        }
        latLngsArray = tempLat;
        polygon.setPoints(tempLat);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions mMarkerOptions = new MarkerOptions().position(latLng).draggable(true);
        Marker marker= mMap.addMarker(mMarkerOptions);
        markerArray.add(marker);

        if (polygon == null){
            clear_button.setVisibility(View.VISIBLE);
            clear_button_test = true;
            polygonOptions.add(latLng);
            polygon = mMap.addPolygon(polygonOptions);
        }else{
            update_polygon();
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        update_polygon();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        update_polygon();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        update_polygon();
    }


    private void zoomToCentroid(){

        /*
            Calculate Centroid of Polygon
         */

        Handler handler=new Handler();
        Runnable r = new Runnable(){
            public void run() {
                Integer size  = markerArray.size();
                Double x_value = 0.0;
                Double y_value = 0.0;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(int i=0; i<size; i++){
                    LatLng temp_marker = markerArray.get(i).getPosition();
                    Double x_marker = temp_marker.latitude;
                    Double y_marker = temp_marker.longitude;
                    LatLng ll = new LatLng(x_marker,y_marker);
                    builder.include(ll);
                    x_value += x_marker;
                    y_value += y_marker;
                }

                Double x_cord = x_value/size;
                Double y_cord = y_value/size;
                LatLng centroid = new LatLng(x_cord,y_cord);
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,width, height,20));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centroid,16));

            }
        };

        handler.post(r);

    }
    private void clearFeatures(){
        // Clear all the features
        mMap.clear();
        clear_button.setVisibility(View.GONE);
        clear_button_test = false;
        polygon = null;
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);
        markerArray.clear();

    }
    private void showClearDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.geo_clear_warning))
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
}
