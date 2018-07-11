/*
 * Copyright (C) 2015 GeoODK
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
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.android.R;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoTraceWidget;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfLocationPermissionsGranted;

public class GeoTraceOsmMapActivity extends CollectAbstractActivity implements IRegisterReceiver,
        LocationListener, LocationClient.LocationClientListener {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture schedulerHandler;
    public int zoomLevel = 3;
    public boolean gpsStatus = true;
    private boolean playCheck;
    private MapView mapView;
    public MyLocationNewOverlay myLocationOverlay;
    private ImageButton locationButton;
    private ImageButton playButton;
    public ImageButton layersButton;
    public ImageButton clearButton;
    private Button manualCaptureButton;
    private ImageButton pauseButton;
    public AlertDialog.Builder builder;
    public AlertDialog.Builder polylineAlertBuilder;
    public LayoutInflater inflater;
    private AlertDialog alert;
    private AlertDialog alertDialog;
    private View traceSettingsView;
    private View polygonPolylineView;
    private Polyline polyline;
    private final ArrayList<Marker> mapMarkers = new ArrayList<Marker>();
    private Integer traceMode; // 0 manual, 1 is automatic
    private Spinner timeUnits;
    private Spinner timeDelay;
    private boolean beenPaused;
    private MapHelper helper;

    private AlertDialog zoomDialog;
    private AlertDialog errorDialog;

    private View zoomDialogView;
    private Button zoomPointButton;
    private Button zoomLocationButton;
    private boolean modeActive;

    private LocationClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIfLocationPermissionsGranted(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geotrace_osm_layout);
        setTitle(getString(R.string.geotrace_title)); // Setting title of the action

        // For testing purposes:
        if (mapView == null) {
            mapView = findViewById(R.id.geotrace_mapview);
        }

        if (helper == null) {
            helper = new MapHelper(this, mapView, this);
        }

        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(mapView);
        }

        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.getController().setZoom(zoomLevel);

        inflater = this.getLayoutInflater();
        traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
        polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
        timeDelay = traceSettingsView.findViewById(R.id.trace_delay);
        timeDelay.setSelection(3);
        timeUnits = traceSettingsView.findViewById(R.id.trace_scale);
        layersButton = findViewById(R.id.layers);
        layersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                helper.showLayersDialog();

            }
        });

        locationButton = findViewById(R.id.show_location);
        locationButton.setEnabled(false);
        locationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset_trace_settings();
                showZoomDialog();
            }

        });

        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showClearDialog();

            }

        });

        ImageButton saveButton = findViewById(R.id.geotrace_save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mapMarkers.isEmpty()) {
                    alertDialog.show();
                } else {
                    saveGeoTrace();
                }
            }
        });
        if (mapMarkers.isEmpty()) {
            clearButton.setEnabled(false);
        }
        manualCaptureButton = findViewById(R.id.manual_button);
        manualCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationMarker();
            }
        });
        pauseButton = findViewById(R.id.pause);
        playButton = findViewById(R.id.play);
        playButton.setEnabled(false);
        beenPaused = false;
        traceMode = 1;


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!playCheck) {
                    if (!beenPaused) {
                        alert.show();
                    } else {
                        RadioGroup rb = traceSettingsView.findViewById(
                                R.id.radio_group);
                        int radioButtonID = rb.getCheckedRadioButtonId();
                        View radioButton = rb.findViewById(radioButtonID);
                        traceMode = rb.indexOfChild(radioButton);
                        if (traceMode == 0) {
                            setupManualMode();
                        } else if (traceMode == 1) {
                            setupAutomaticMode();
                        } else {
                            reset_trace_settings();
                        }
                    }
                    playCheck = true;
                } else {
                    playCheck = false;
                    startGeoTrace();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                playButton.setVisibility(View.VISIBLE);
                if (!mapMarkers.isEmpty()) {
                    clearButton.setEnabled(true);
                }
                pauseButton.setVisibility(View.GONE);
                manualCaptureButton.setVisibility(View.GONE);
                playCheck = true;
                modeActive = false;
                myLocationOverlay.disableFollowLocation();

                try {
                    schedulerHandler.cancel(true);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        });

        overlayMapLayerListener();
        buildDialogs();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
                String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
                playButton.setEnabled(false);
                clearButton.setEnabled(true);
                overlayIntentTrace(s);
                locationButton.setEnabled(true);
                //zoomToCentroid();
                zoomToBounds();

            }
        } else {
            myLocationOverlay.runOnFirstFix(centerAroundFix);
        }


        Button polygonSaveButton = polygonPolylineView.findViewById(R.id.polygon_save);
        polygonSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mapMarkers.size() > 2) {
                    createPolygon();
                    alertDialog.dismiss();
                    saveGeoTrace();
                } else {
                    alertDialog.dismiss();
                    ToastUtils.showShortToastInMiddle(getString(R.string.polygon_validator));
                }


            }
        });
        Button polylineSaveButton = polygonPolylineView.findViewById(R.id.polyline_save);
        polylineSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mapMarkers.size() > 1) {
                    alertDialog.dismiss();
                    saveGeoTrace();
                } else {
                    alertDialog.dismiss();
                    ToastUtils.showShortToastInMiddle(getString(R.string.polyline_validator));
                }
            }
        });

        zoomDialogView = getLayoutInflater().inflate(R.layout.geo_zoom_dialog, null);

        zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zoomToMyLocation();
                mapView.invalidate();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = zoomDialogView.findViewById(R.id.zoom_saved_location);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //zoomToCentroid();
                zoomToBounds();
                mapView.invalidate();
                zoomDialog.dismiss();
            }
        });


        mapView.invalidate();

        locationClient = LocationClients.clientForContext(this);
        locationClient.setListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            helper.setBasemap();
        }
    }

    @Override
    protected void onStop() {
        locationClient.stop();
        super.onStop();
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (schedulerHandler != null && !schedulerHandler.isCancelled()) {
            schedulerHandler.cancel(true);
        }
        super.onDestroy();
    }

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

    public void overlayIntentTrace(String str) {
        String s = str.replace("; ", ";");
        for (String sa : s.split(";")) {
            String[] sp = sa.split(" ");
            double[] gp = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            String altStr = sp[2].replace(" ", "");
            String acu = sp[3].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            Double alt = Double.parseDouble(altStr);
            Marker marker = new Marker(mapView);
            marker.setSubDescription(acu);
            GeoPoint point = new GeoPoint(gp[0], gp[1]);
            point.setAltitude(alt.intValue());
            marker.setPosition(point);
            marker.setOnMarkerClickListener(nullMarkerListener);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(dragListener);
            marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_black));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapMarkers.add(marker);
            List<GeoPoint> points = polyline.getPoints();
            points.add(marker.getPosition());
            polyline.setPoints(points);
            mapView.getOverlays().add(marker);

        }
        mapView.invalidate();

    }

    private void disableMyLocation() {
        if (!locationClient.isLocationAvailable()) {
            myLocationOverlay.setEnabled(false);
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
            gpsStatus = false;
        }
    }

    private void upMyLocationOverlayLayers() {
        if (locationClient.isLocationAvailable()) {
            overlayMyLocationLayers();

        } else {
            showGPSDisabledAlertToUser();
        }
    }

    private void overlayMapLayerListener() {
        polyline = new Polyline();
        polyline.setColor(Color.RED);
        Paint paint = polyline.getPaint();
        paint.setStrokeWidth(5);
        mapView.getOverlays().add(polyline);
        mapView.invalidate();
    }

    private void overlayMyLocationLayers() {
        //myLocationOverlay.runOnFirstFix(centerAroundFix);
        //if(myLocationOverlay.getMyLocation()!= null){
        //myLocationOverlay.runOnFirstFix(centerAroundFix);
        //}
        mapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.setEnabled(true);
        myLocationOverlay.enableMyLocation();


    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable centerAroundFix = new Runnable() {
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    locationButton.setEnabled(true);
                    playButton.setEnabled(true);
                    showZoomDialog();
                }
            });
        }
    };


    private void zoomToMyLocation() {
        if (myLocationOverlay.getMyLocation() != null) {
            if (zoomLevel == 3) {
                mapView.getController().setZoom(15);
            } else {
                mapView.getController().setZoom(zoomLevel);
            }
            mapView.getController().setCenter(myLocationOverlay.getMyLocation());
        } else {
            mapView.getController().setZoom(zoomLevel);
        }

    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.enable_gps_message))
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
        errorDialog = alertDialogBuilder.create();
        errorDialog.show();
    }

    //This happens on click of the play button
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


    private void buildDialogs() {

        builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.select_geotrace_mode));
        builder.setView(null);
        builder.setView(traceSettingsView)
                // Add action buttons
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
                        reset_trace_settings();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        reset_trace_settings();
                    }
                });


        alert = builder.create();


        polylineAlertBuilder = new AlertDialog.Builder(this);
        polylineAlertBuilder.setTitle(getString(R.string.polyline_polygon_text));
        polylineAlertBuilder.setView(polygonPolylineView)
                // Add action buttons
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                })
                .setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        alert.dismiss();
                    }
                });

        alertDialog = polylineAlertBuilder.create();


    }


    private void reset_trace_settings() {
        playCheck = false;
    }

    private void startGeoTrace() {
        RadioGroup rb = traceSettingsView.findViewById(R.id.radio_group);
        int radioButtonID = rb.getCheckedRadioButtonId();
        View radioButton = rb.findViewById(radioButtonID);
        int idx = rb.indexOfChild(radioButton);
        beenPaused = true;
        traceMode = idx;
        if (traceMode == 0) {
            setupManualMode();
        } else if (traceMode == 1) {
            setupAutomaticMode();
        } else {
            reset_trace_settings();
        }
        playButton.setVisibility(View.GONE);
        clearButton.setEnabled(false);
        pauseButton.setVisibility(View.VISIBLE);


    }

    private void setupManualMode() {
        manualCaptureButton.setVisibility(View.VISIBLE);
        modeActive = true;

    }

    private void setupAutomaticMode() {
        manualCaptureButton.setVisibility(View.VISIBLE);
        String delay = timeDelay.getSelectedItem().toString();
        String units = timeUnits.getSelectedItem().toString();
        Long timeDelay;
        TimeUnit timeUnitsValue;
        if (units.equals(getString(R.string.minutes))) {
            timeDelay = Long.parseLong(delay) * (60); //Convert minutes to seconds
            timeUnitsValue = TimeUnit.SECONDS;
        } else {
            //in Seconds
            timeDelay = Long.parseLong(delay);
            timeUnitsValue = TimeUnit.SECONDS;
        }

        setGeoTraceScheduler(timeDelay, timeUnitsValue);
        modeActive = true;
    }

    private void addLocationMarker() {
        if (myLocationOverlay.getMyLocation() != null) {
            Marker marker = new Marker(mapView);
            marker.setPosition(myLocationOverlay.getMyLocation());
            Float lastKnownAccuracy =
                    myLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
            myLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
            marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_black));
            marker.setSubDescription(Float.toString(lastKnownAccuracy));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(dragListener);
            mapMarkers.add(marker);

            marker.setOnMarkerClickListener(nullMarkerListener);
            mapView.getOverlays().add(marker);
            List<GeoPoint> points = polyline.getPoints();
            points.add(marker.getPosition());
            polyline.setPoints(points);
            mapView.invalidate();
        }
    }

    private void saveGeoTrace() {
        returnLocation();
        finish();
    }

    private String generateReturnString() {
        String tempString = "";
        for (int i = 0; i < mapMarkers.size(); i++) {
            String lat = Double.toString(mapMarkers.get(i).getPosition().getLatitude());
            String lng = Double.toString(mapMarkers.get(i).getPosition().getLongitude());
            String alt = Double.toString(mapMarkers.get(i).getPosition().getAltitude());
            String acu = mapMarkers.get(i).getSubDescription();
            tempString = tempString + lat + " " + lng + " " + alt + " " + acu + ";";
        }
        return tempString;
    }

    private void returnLocation() {
        String finalReturnString = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOTRACE_RESULTS,
                finalReturnString);
        setResult(RESULT_OK, i);
    }

    private final Marker.OnMarkerClickListener nullMarkerListener = new Marker.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    private void createPolygon() {
        mapMarkers.add(mapMarkers.get(0));
        List<GeoPoint> points = polyline.getPoints();
        points.add(mapMarkers.get(0).getPosition());
        polyline.setPoints(points);
        mapView.invalidate();
    }

    private void update_polygon() {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < mapMarkers.size(); i++) {
            points.add(mapMarkers.get(i).getPosition());
        }
        polyline.setPoints(points);
        mapView.invalidate();
    }


    private final Marker.OnMarkerDragListener dragListener = new Marker.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker arg0) {
            update_polygon();

        }

        @Override
        public void onMarkerDrag(Marker marker) {
            update_polygon();
            marker.setSubDescription("0.0");
        }
    };

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

    private void clearFeatures() {
        mapMarkers.clear();
        polyline.setPoints(new ArrayList<GeoPoint>());
        mapView.getOverlays().clear();
        clearButton.setEnabled(false);
        overlayMyLocationLayers();
        overlayMapLayerListener();
        mapView.invalidate();
        playButton.setEnabled(true);
        modeActive = false;
    }

    private void zoomToBounds() {
        mapView.getController().setZoom(4);
        mapView.invalidate();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                double minLat = Double.MAX_VALUE;
                double maxLat = Double.MIN_VALUE;
                double minLong = Double.MAX_VALUE;
                double maxLong = Double.MIN_VALUE;
                Integer size = mapMarkers.size();
                for (int i = 0; i < size; i++) {
                    GeoPoint tempMarker = mapMarkers.get(i).getPosition();
                    if (tempMarker.getLatitude() < minLat) {
                        minLat = tempMarker.getLatitude();
                    }
                    if (tempMarker.getLatitude() > maxLat) {
                        maxLat = tempMarker.getLatitude();
                    }
                    if (tempMarker.getLongitude() < minLong) {
                        minLong = tempMarker.getLongitude();
                    }
                    if (tempMarker.getLongitude() > maxLong) {
                        maxLong = tempMarker.getLongitude();
                    }
                }
                BoundingBox boundingBox = new BoundingBox(maxLat, maxLong, minLat, minLong);
                mapView.zoomToBoundingBox(boundingBox, false);
                mapView.invalidate();
            }
        }, 100);
        mapView.invalidate();

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

        if (myLocationOverlay.getMyLocation() != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(themeUtils.getPrimaryTextColor());
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }
        //If feature enable zoom to button else disable
        if (!mapMarkers.isEmpty()) {
            zoomPointButton.setEnabled(true);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomPointButton.setTextColor(themeUtils.getPrimaryTextColor());
        } else {
            zoomPointButton.setEnabled(false);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomPointButton.setTextColor(Color.parseColor("#FF979797"));
        }
        zoomDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (modeActive && myLocationOverlay.getMyLocation() != null) {
            mapView.getController().setCenter(myLocationOverlay.getMyLocation());
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);
        upMyLocationOverlayLayers();
    }

    @Override
    public void onClientStartFailure() {
        showGPSDisabledAlertToUser();
    }

    @Override
    public void onClientStop() {
        disableMyLocation();
    }

    public void setModeActive(boolean modeActive) {
        this.modeActive = modeActive;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public void setHelper(MapHelper helper) {
        this.helper = helper;
    }

    public void setMyLocationOverlay(MyLocationNewOverlay myLocationOverlay) {
        this.myLocationOverlay = myLocationOverlay;
    }

    public AlertDialog getErrorDialog() {
        return errorDialog;
    }
}
