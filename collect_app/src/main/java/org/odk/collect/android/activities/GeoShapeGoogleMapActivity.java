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

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.ArrayList;
import java.util.List;

/**
 * Version of the GeoShapeGoogleMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
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

    }

//    private void stopGeolocating() {
//
//    }

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

    private void returnLocation() {
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Log.w("onMarkerDrag","IS HAPPENING");
        update_polygon();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.w("onMarkerDragEnd","IS HAPPENING");
        update_polygon();
    }
}
