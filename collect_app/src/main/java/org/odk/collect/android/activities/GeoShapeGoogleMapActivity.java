/*
 * Copyright (C) 2016 Nafundi
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
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoShapeWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Version of the GeoShapeGoogleMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 */

public class GeoShapeGoogleMapActivity extends FragmentActivity implements LocationListener,
        OnMarkerDragListener, OnMapLongClickListener {

    private GoogleMap map;
    private LocationManager locationManager;
    private Boolean gpsOn = false;
    private Boolean networkOn = false;
    private Location curLocation;
    private LatLng curlatLng;
    private PolygonOptions polygonOptions;
    private Polygon polygon;
    private ArrayList<Marker> markerArray = new ArrayList<Marker>();
    private ImageButton gpsButton;
    private ImageButton clearButton;

    private MapHelper helper;
    private AlertDialog zoomDialog;
    private View zoomDialogView;
    private Button zoomPointButton;
    private Button zoomLocationButton;
    private Boolean foundFirstLocation = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geoshape_google_layout);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void setupMap(GoogleMap googleMap) {
        map = googleMap;
        if (map == null) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }
        helper = new MapHelper(GeoShapeGoogleMapActivity.this, map);
        map.setMyLocationEnabled(true);
        map.setOnMapLongClickListener(GeoShapeGoogleMapActivity.this);
        map.setOnMarkerDragListener(GeoShapeGoogleMapActivity.this);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);

        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);
        polygonOptions.zIndex(1);

        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gpsOn = true;
                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                networkOn = true;
                curLocation = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
            }
        }

        if (!gpsOn & !networkOn) {
            showGPSDisabledAlertToUser();
        }

        gpsButton = (ImageButton) findViewById(R.id.gps);
        gpsButton.setEnabled(false);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if(curLocation !=null){
                //    map.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng,16));
                // }
                showZoomDialog();
            }
        });

        clearButton = (ImageButton) findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0) {
                    showClearDialog();
                }
            }
        });
        ImageButton returnButton = (ImageButton) findViewById(R.id.save);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoShapeWidget.SHAPE_LOCATION)) {
                clearButton.setEnabled(true);
                String s = intent.getStringExtra(GeoShapeWidget.SHAPE_LOCATION);
                gpsButton.setEnabled(true);
                overlayIntentPolygon(s);
            }
        }

        ImageButton layersButton = (ImageButton) findViewById(R.id.layers);
        layersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.showLayersDialog(GeoShapeGoogleMapActivity.this);
            }
        });

        zoomDialogView = getLayoutInflater().inflate(R.layout.geoshape_zoom_dialog, null);

        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curLocation != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng, 17));
                }
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_shape);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // zoomToCentroid();
                zoomToBounds();
                zoomDialog.dismiss();
            }
        });
        // If there is a last know location go there
        if (curLocation != null) {
            curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
            foundFirstLocation = true;
            gpsButton.setEnabled(true);
            showZoomDialog();
        }

        helper.setBasemap();
        providers = locationManager.getProviders(true);

        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gpsOn = true;
                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                // gpsButton.setEnabled(true);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                networkOn = true;
                curLocation = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
                // gpsButton.setEnabled(true);
            }
        }

        if (gpsOn) {
            // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            gpsButton.setEnabled(true);
        }
        if (networkOn) {
            // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            gpsButton.setEnabled(true);
        }
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


    private void returnLocation() {
        String finalReturnString = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                finalReturnString);
        setResult(RESULT_OK, i);
        finish();
    }

    private void overlayIntentPolygon(String str) {
        map.setOnMapLongClickListener(null);
        clearButton.setEnabled(true);
        String s = str.replace("; ", ";");
        String[] sa = s.split(";");
        for (int i = 0; i < (sa.length - 1); i++) {
            String[] sp = sa[i].split(" ");
            double[] gp = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            LatLng point = new LatLng(gp[0], gp[1]);
            polygonOptions.add(point);
            MarkerOptions markerOptions = new MarkerOptions().position(point).draggable(true);
            Marker marker = map.addMarker(markerOptions);
            markerArray.add(marker);
        }
        polygon = map.addPolygon(polygonOptions);
        update_polygon();

    }

    private String generateReturnString() {
        String tempString = "";
        //Add the first marker to the end of the array, so the first and the last are the same
        if (markerArray.size() > 1) {
            markerArray.add(markerArray.get(0));
            for (int i = 0; i < markerArray.size(); i++) {
                String lat = Double.toString(markerArray.get(i).getPosition().latitude);
                String lng = Double.toString(markerArray.get(i).getPosition().longitude);
                String alt = "0.0";
                String acu = "0.0";
                tempString = tempString + lat + " " + lng + " " + alt + " " + acu + ";";
            }
        }
        return tempString;
    }

    @Override
    public void onLocationChanged(Location location) {
        // If there is a location allow for user to be able to fly there
        gpsButton.setEnabled(true);
        curLocation = location;
        curLocation = location;
        curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        if (!foundFirstLocation) {
            showZoomDialog();
            foundFirstLocation = true;
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

    private void update_polygon() {
        ArrayList<LatLng> tempLat = new ArrayList<LatLng>();
        for (int i = 0; i < markerArray.size(); i++) {
            LatLng latLng = markerArray.get(i).getPosition();
            tempLat.add(latLng);
        }
        polygon.setPoints(tempLat);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true);
        Marker marker = map.addMarker(markerOptions);
        markerArray.add(marker);

        if (polygon == null) {
            clearButton.setEnabled(true);
            polygonOptions.add(latLng);
            polygon = map.addPolygon(polygonOptions);
        } else {
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

    private void zoomToBounds() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markerArray) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 200; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);
            }
        }, 100);

    }


    private void clearFeatures() {
        map.clear();
        polygon = null;
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);
        polygonOptions.zIndex(1);
        markerArray.clear();
        map.setOnMapLongClickListener(this);

    }

    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.geo_clear_warning))
                .setPositiveButton(getString(R.string.clear),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clearFeatures();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                }).show();

    }

    public void showZoomDialog() {
        if (zoomDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.zoom_to_where));
            builder.setView(zoomDialogView)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                            zoomDialog.dismiss();
                        }
                    });
            zoomDialog = builder.create();
        }

        if (curLocation != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }

        if (markerArray.size() != 0) {
            zoomPointButton.setEnabled(true);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomPointButton.setTextColor(Color.parseColor("#ff333333"));
        } else {
            zoomPointButton.setEnabled(false);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomPointButton.setTextColor(Color.parseColor("#FF979797"));
        }
        zoomDialog.show();
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_gps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivityForResult(
                                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
