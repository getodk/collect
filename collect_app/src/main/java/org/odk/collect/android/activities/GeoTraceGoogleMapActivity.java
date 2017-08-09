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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoTraceWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Version of the GeoTraceMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 */
public class GeoTraceGoogleMapActivity extends FragmentActivity implements LocationListener,
        OnMarkerDragListener, OnMapLongClickListener {
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture schedulerHandler;
    private ImageButton playButton;
    public ImageButton layersButton;
    public ImageButton clearButton;
    private Button manualButton;
    private ImageButton pauseButton;
    public AlertDialog.Builder builder;
    private View traceSettingsView;
    public LayoutInflater inflater;
    private AlertDialog alert;
    public AlertDialog.Builder dialogBuilder;
    private View polygonPolylineView;
    private AlertDialog alertDialog;
    private Boolean beenPaused = false;
    private Integer traceMode = 1; // 0 manual, 1 is automatic
    private Boolean playCheck = false;
    private Spinner timeUnits;
    private Spinner timeDelay;

    private GoogleMap map;
    private LocationManager locationManager;
    private Boolean gpsOn = false;
    private Boolean networkOn = false;
    private Location curLocation;
    private LatLng curlatLng;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private ArrayList<Marker> markerArray = new ArrayList<Marker>();
    private MapHelper helper;

    private AlertDialog zoomDialog;

    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    private Boolean firstLocationFound = false;
    private Boolean modeActive = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geotrace_google_layout);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
    }

    private void setupMap(GoogleMap googleMap) {
        map = googleMap;
        if (map == null) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        helper = new MapHelper(GeoTraceGoogleMapActivity.this, map);
        map.setMyLocationEnabled(true);
        map.setOnMapLongClickListener(GeoTraceGoogleMapActivity.this);
        map.setOnMarkerDragListener(GeoTraceGoogleMapActivity.this);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);

        clearButton = (ImageButton) findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0) {
                    showClearDialog();
                }
            }
        });

        inflater = this.getLayoutInflater();
        traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
        polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
        timeDelay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
        timeDelay.setSelection(3);
        timeUnits = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
        pauseButton = (ImageButton) findViewById(R.id.pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                playButton.setVisibility(View.VISIBLE);
                if (markerArray != null && markerArray.size() > 0) {
                    clearButton.setEnabled(true);
                }
                pauseButton.setVisibility(View.GONE);
                manualButton.setVisibility(View.GONE);
                playCheck = true;
                modeActive = false;
                try {
                    schedulerHandler.cancel(true);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        });
        layersButton = (ImageButton) findViewById(R.id.layers);


        ImageButton saveButton = (ImageButton) findViewById(R.id.geotrace_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0) {
                    alertDialog.show();
                } else {
                    saveGeoTrace();
                }
            }
        });
        playButton = (ImageButton) findViewById(R.id.play);
        playButton.setEnabled(false);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!playCheck) {
                    if (!beenPaused) {
                        alert.show();
                    } else {
                        RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(
                                R.id.radio_group);
                        int radioButtonID = rb.getCheckedRadioButtonId();
                        View radioButton = rb.findViewById(radioButtonID);
                        traceMode = rb.indexOfChild(radioButton);
                        if (traceMode == 0) {
                            setupManualMode();
                        } else if (traceMode == 1) {
                            setupAutomaticMode();
                        } else {
                            //Do nothing
                        }
                    }
                    playCheck = true;
                } else {
                    playCheck = false;
                    startGeoTrace();
                }
            }
        });

        if (markerArray == null || markerArray.size() == 0) {
            clearButton.setEnabled(false);
        }

        manualButton = (Button) findViewById(R.id.manual_button);
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationMarker();
            }
        });

        Button polygonSave = (Button) polygonPolylineView.findViewById(R.id.polygon_save);
        polygonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() > 2) {
                    createPolygon();
                    alertDialog.dismiss();
                    saveGeoTrace();
                } else {
                    alertDialog.dismiss();
                    showPolygonErrorDialog();
                }
            }
        });
        Button polylineSave = (Button) polygonPolylineView.findViewById(R.id.polyline_save);
        polylineSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                saveGeoTrace();
            }
        });

        buildDialogs();

        layersButton = (ImageButton) findViewById(R.id.layers);
        layersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.showLayersDialog(GeoTraceGoogleMapActivity.this);
            }
        });

        ImageButton locationButton = (ImageButton) findViewById(R.id.show_location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showZoomDialog();
            }
        });

        zoomDialogView = getLayoutInflater().inflate(R.layout.geoshape_zoom_dialog, null);

        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToMyLocation();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_shape);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToBounds();
                zoomDialog.dismiss();
            }
        });
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

        if (gpsOn) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    GeoTraceGoogleMapActivity.this);
        }
        if (networkOn) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                    GeoTraceGoogleMapActivity.this);
        }

        if (!gpsOn & !networkOn) {
            showGPSDisabledAlertToUser();
        }
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
                playButton.setEnabled(false);
                clearButton.setEnabled(true);
                firstLocationFound = true;
                locationButton.setEnabled(true);
                String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
                overlayIntentTrace(s);
                zoomToBounds();
            }
        } else {
            if (curLocation != null) {
                curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
            }
        }

        helper.setBasemap();
    }

    private void overlayIntentTrace(String str) {
        map.setOnMapLongClickListener(null);
        String s = str.replace("; ", ";");
        for (String sa : s.split(";")) {
            String[] sp = sa.split(" ");
            double[] gp = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            LatLng point = new LatLng(gp[0], gp[1]);
            polylineOptions.add(point);
            MarkerOptions markerOptions = new MarkerOptions().position(point).draggable(true);
            Marker marker = map.addMarker(markerOptions);
            markerArray.add(marker);
        }
        polyline = map.addPolyline(polylineOptions);
        update_polyline();

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
        disableMyLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (schedulerHandler != null && !schedulerHandler.isCancelled()) {
            schedulerHandler.cancel(true);
        }
    }

    private void returnLocation() {

        String finalReturnString = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOTRACE_RESULTS,
                finalReturnString);
        setResult(RESULT_OK, i);
        finish();
    }

    private void disableMyLocation() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private String generateReturnString() {
        String tempString = "";
        for (int i = 0; i < markerArray.size(); i++) {
            String lat = Double.toString(markerArray.get(i).getPosition().latitude);
            String lng = Double.toString(markerArray.get(i).getPosition().longitude);
            String alt = "0.0";
            String acu = "0.0";
            tempString = tempString + lat + " " + lng + " " + alt + " " + acu + ";";
        }
        return tempString;
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    private void buildDialogs() {

        builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.select_geotrace_mode));
        builder.setView(null);
        builder.setView(traceSettingsView)
                .setPositiveButton(getString(R.string.start),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                startGeoTrace();
                                dialog.cancel();
                                alert.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        alert.dismiss();
                        playCheck = false;

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        playCheck = false;

                    }
                });


        alert = builder.create();

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.polygon_or_polyline));
        dialogBuilder.setView(polygonPolylineView)

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        alert.dismiss();
                    }
                });

        alertDialog = dialogBuilder.create();

        zoomDialogView = getLayoutInflater().inflate(R.layout.geoshape_zoom_dialog, null);

        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zoomToMyLocation();

                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_shape);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                zoomToBounds();

                zoomDialog.dismiss();
            }
        });


    }

    private void startGeoTrace() {
        RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
        int radioButtonID = rb.getCheckedRadioButtonId();
        View radioButton = rb.findViewById(radioButtonID);
        int idx = rb.indexOfChild(radioButton);
        beenPaused = true;
        traceMode = idx;
        if (traceMode == 0) {
            setupManualMode();
        } else if (traceMode == 1) {
            setupAutomaticMode();
        }
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);


    }

    private void setupManualMode() {

        manualButton.setVisibility(View.VISIBLE);
        modeActive = true;

    }

    private void setupAutomaticMode() {
        manualButton.setVisibility(View.VISIBLE);
        String delay = timeDelay.getSelectedItem().toString();
        String units = timeUnits.getSelectedItem().toString();
        Long timeDelay;
        TimeUnit timeUnitsValue;
        if (units == getString(R.string.minutes)) {
            timeDelay = Long.parseLong(delay) * (60);
            timeUnitsValue = TimeUnit.SECONDS;

        } else {
            //in Seconds
            timeDelay = Long.parseLong(delay);
            timeUnitsValue = TimeUnit.SECONDS;
        }

        setGeoTraceScheduler(timeDelay, timeUnitsValue);
        modeActive = true;
    }

    public void setGeoTraceMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.trace_manual:
                if (checked) {
                    traceMode = 0;
                    timeUnits.setVisibility(View.GONE);
                    timeDelay.setVisibility(View.GONE);
                    timeDelay.invalidate();
                    timeUnits.invalidate();
                }
                break;
            case R.id.trace_automatic:
                if (checked) {
                    traceMode = 1;
                    timeUnits.setVisibility(View.VISIBLE);
                    timeDelay.setVisibility(View.VISIBLE);
                    timeDelay.invalidate();
                    timeUnits.invalidate();
                }
                break;
        }
    }

    private void createPolygon() {
        markerArray.add(markerArray.get(0));
        update_polyline();
    }

    /*
            This functions handles the delay and the Runnable for
    */

    public void setGeoTraceScheduler(long delay, TimeUnit units) {
        schedulerHandler = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addLocationMarker();
                    }
                });
            }
        }, delay, delay, units);

    }

    private void update_polyline() {
        ArrayList<LatLng> tempLat = new ArrayList<LatLng>();
        for (int i = 0; i < markerArray.size(); i++) {
            LatLng latLng = markerArray.get(i).getPosition();
            tempLat.add(latLng);
        }
        polyline.setPoints(tempLat);
    }

    private void addLocationMarker() {
        if (curLocation == null) {
            // avoid app crash
            return;
        }
        LatLng latLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true);
        Marker marker = map.addMarker(markerOptions);
        markerArray.add(marker);
        if (polyline == null) {
            polylineOptions.add(latLng);
            polyline = map.addPolyline(polylineOptions);
        } else {
            update_polyline();
        }


    }

    private void saveGeoTrace() {
        returnLocation();
        finish();
    }

    private void zoomToMyLocation() {
        if (curLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng, 17));
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        curLocation = location;
        if (!firstLocationFound) {
            firstLocationFound = true;
            playButton.setEnabled(true);
            showZoomDialog();

        }
        curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        if (modeActive) {
            map.animateCamera(CameraUpdateFactory.newLatLng(curlatLng));
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

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        update_polyline();

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        update_polyline();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        update_polyline();
    }

    private void showPolygonErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.polygon_validator))
                .setPositiveButton(getString(R.string.dialog_continue),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).show();

    }

    private void clearFeatures() {

        map.clear();
        modeActive = false;
        clearButton.setEnabled(false);
        polyline = null;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        markerArray.clear();
        pauseButton.setVisibility(View.GONE);
        clearButton.setEnabled(false);
        manualButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
        playButton.setEnabled(true);
        playCheck = false;
        beenPaused = false;


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

                    }
                }).show();

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
}
