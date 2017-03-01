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

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.odk.collect.android.R;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.widgets.GeoTraceWidget;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class GeoTraceOsmMapActivity extends AppCompatActivity implements IRegisterReceiver,
        LocationListener {
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture schedulerHandler;
    public int zoom_level = 3;
    public Boolean gpsStatus = true;
    private Boolean play_check = false;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    public DefaultResourceProxyImpl resource_proxy;
    public MyLocationNewOverlay mMyLocationOverlay;
    private Button mLocationButton;
    private Button mPlayButton;
    private Button mSaveButton;
    public Button mLayersButton;
    public Button mClearButton;
    private Button mManualCaptureButton;
    private Button mPauseButton;
    public AlertDialog.Builder builder;
    public AlertDialog.Builder p_builder;
    public LayoutInflater inflater;
    private AlertDialog alert;
    private AlertDialog p_alert;
    private View traceSettingsView;
    private View polygonPolylineView;
    private PathOverlay pathOverlay;
    private ArrayList<Marker> map_markers = new ArrayList<Marker>();
    private String final_return_string;
    private Integer TRACE_MODE; // 0 manual, 1 is automatic
    private Boolean inital_location_found = false;
    private Spinner time_units;
    private Spinner time_delay;
    private Button mPolygonSaveButton;
    private Button mPolylineSaveButton;
    private Boolean beenPaused;
    private MapHelper mHelper;

    private AlertDialog zoomDialog;
    private View zoomDialogView;
    private LocationManager mLocationManager;
    private Button zoomPointButton;
    private Button zoomLocationButton;
    private Boolean mode_active = false;
    private Boolean mGPSOn = false;
    private Boolean mNetworkOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geotrace_osm_layout);
        setTitle(getString(R.string.geotrace_title)); // Setting title of the action
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
        mapView = (MapView) findViewById(R.id.geotrace_mapview);
        mHelper = new MapHelper(this, mapView, GeoTraceOsmMapActivity.this);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(zoom_level);
        mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);

        inflater = this.getLayoutInflater();
        traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
        polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
        time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
        time_delay.setSelection(3);
        time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
        mLayersButton = (Button) findViewById(R.id.layers);
        mLayersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog(GeoTraceOsmMapActivity.this);

            }
        });

        mLocationButton = (Button) findViewById(R.id.show_location);
        mLocationButton.setEnabled(false);
        mLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset_trace_settings();
                showZoomDialog();
            }

        });

        mClearButton = (Button) findViewById(R.id.clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showClearDialog();

            }

        });

        mSaveButton = (Button) findViewById(R.id.geotrace_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (map_markers.size() != 0) {
                    p_alert.show();
                } else {
                    saveGeoTrace();
                }
            }
        });
        if (map_markers == null || map_markers.size() == 0) {
            mClearButton.setEnabled(false);
        }
        mManualCaptureButton = (Button) findViewById(R.id.manual_button);
        mManualCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationMarker();
            }
        });
        mPauseButton = (Button) findViewById(R.id.pause);
        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(false);
        beenPaused = false;
        TRACE_MODE = 1;


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!play_check) {
                    if (!beenPaused) {
                        alert.show();
                    } else {
                        RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(
                                R.id.radio_group);
                        int radioButtonID = rb.getCheckedRadioButtonId();
                        View radioButton = rb.findViewById(radioButtonID);
                        int idx = rb.indexOfChild(radioButton);
                        TRACE_MODE = idx;
                        if (TRACE_MODE == 0) {
                            setupManualMode();
                        } else if (TRACE_MODE == 1) {
                            setupAutomaticMode();
                        } else {
                            reset_trace_settings();
                        }
                    }
                    play_check = true;
                } else {
                    play_check = false;
                    startGeoTrace();
                }
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mPlayButton.setVisibility(View.VISIBLE);
                if (map_markers != null && map_markers.size() > 0) {
                    mClearButton.setEnabled(true);
                }
                mPauseButton.setVisibility(View.GONE);
                mManualCaptureButton.setVisibility(View.GONE);
                play_check = true;
                mode_active = false;
                mMyLocationOverlay.disableFollowLocation();

                try {
                    schedulerHandler.cancel(true);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        });

        overlayMapLayerListner();
        buildDialogs();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
                String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
                mPlayButton.setEnabled(false);
                mClearButton.setEnabled(true);
                overlayIntentTrace(s);
                mLocationButton.setEnabled(true);
                //zoomToCentroid();
                zoomtoBounds();

            }
        } else {
            mMyLocationOverlay.runOnFirstFix(centerAroundFix);
        }


        mPolygonSaveButton = (Button) polygonPolylineView.findViewById(R.id.polygon_save);
        mPolygonSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (map_markers.size() > 2) {
                    createPolygon();
                    p_alert.dismiss();
                    saveGeoTrace();
                } else {
                    p_alert.dismiss();
                    showPolyonErrorDialog();
                }


            }
        });
        mPolylineSaveButton = (Button) polygonPolylineView.findViewById(R.id.polyline_save);
        mPolylineSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                p_alert.dismiss();
                saveGeoTrace();

            }
        });

        zoomDialogView = getLayoutInflater().inflate(R.layout.geoshape_zoom_dialog, null);

        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zoomToMyLocation();
                mapView.invalidate();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_shape);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //zoomToCentroid();
                zoomtoBounds();
                mapView.invalidate();
                zoomDialog.dismiss();
            }
        });


        mapView.invalidate();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mGPSOn = true;
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                mNetworkOn = true;
            }
        }
        if (mGPSOn) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        if (mNetworkOn) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHelper.setBasemap();
        upMyLocationOverlayLayers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMyLocationOverlay.enableMyLocation();
//		if(mMyLocationOverlay.getMyLocation()!= null){
//			mMyLocationOverlay.runOnFirstFix(centerAroundFix);
//		}

    }

    @Override
    protected void onStop() {
        super.onStop();
        disableMyLocation();
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void setGeoTraceScheuler(long delay, TimeUnit units) {
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
        String[] sa = s.split(";");
        for (int i = 0; i < (sa.length); i++) {
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
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
            marker.setOnMarkerClickListener(nullmarkerlistner);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(draglistner);
            marker.setIcon(getResources().getDrawable(R.drawable.ic_place_black_36dp));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map_markers.add(marker);
            pathOverlay.addPoint(marker.getPosition());
            mapView.getOverlays().add(marker);

        }
        mapView.invalidate();

    }

    private void disableMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            mMyLocationOverlay.setEnabled(false);
            mMyLocationOverlay.disableFollowLocation();
            mMyLocationOverlay.disableMyLocation();
            gpsStatus = false;
        }

    }

    private void upMyLocationOverlayLayers() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            overlayMyLocationLayers();
        } else {
            showGPSDisabledAlertToUser();
        }
    }

    private void overlayMapLayerListner() {
        pathOverlay = new PathOverlay(Color.RED, this);
        Paint pPaint = pathOverlay.getPaint();
        pPaint.setStrokeWidth(5);
        mapView.getOverlays().add(pathOverlay);
        mapView.invalidate();
    }

    private void overlayMyLocationLayers() {
//		mMyLocationOverlay.runOnFirstFix(centerAroundFix);
//		if(mMyLocationOverlay.getMyLocation()!= null){
//			mMyLocationOverlay.runOnFirstFix(centerAroundFix);
//		}
        mapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();


    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable centerAroundFix = new Runnable() {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    mLocationButton.setEnabled(true);
                    mPlayButton.setEnabled(true);
                    showZoomDialog();
                }
            });
        }
    };


    private void zoomToMyLocation() {
        if (mMyLocationOverlay.getMyLocation() != null) {
            inital_location_found = true;
            if (zoom_level == 3) {
                mapView.getController().setZoom(15);
            } else {
                mapView.getController().setZoom(zoom_level);
            }
            mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
        } else {
            mapView.getController().setZoom(zoom_level);
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
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //This happens on click of the play button
    public void setGeoTraceMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.trace_manual:
                if (checked) {
                    TRACE_MODE = 0;
                    time_units.setVisibility(View.GONE);
                    time_delay.setVisibility(View.GONE);
                    time_delay.invalidate();
                    time_units.invalidate();
                }
                break;
            case R.id.trace_automatic:
                if (checked) {
                    TRACE_MODE = 1;
                    time_units.setVisibility(View.VISIBLE);
                    time_delay.setVisibility(View.VISIBLE);
                    time_delay.invalidate();
                    time_units.invalidate();
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


        p_builder = new AlertDialog.Builder(this);
        p_builder.setTitle(getString(R.string.polyline_polygon_text));
        p_builder.setView(polygonPolylineView)
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

        p_alert = p_builder.create();


    }


    private void reset_trace_settings() {
        play_check = false;
    }

    private void startGeoTrace() {
        RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
        int radioButtonID = rb.getCheckedRadioButtonId();
        View radioButton = rb.findViewById(radioButtonID);
        int idx = rb.indexOfChild(radioButton);
        beenPaused = true;
        TRACE_MODE = idx;
        if (TRACE_MODE == 0) {
            setupManualMode();
        } else if (TRACE_MODE == 1) {
            setupAutomaticMode();
        } else {
            reset_trace_settings();
        }
        mPlayButton.setVisibility(View.GONE);
        mClearButton.setEnabled(false);
        mPauseButton.setVisibility(View.VISIBLE);


    }

    private void setupManualMode() {
        mManualCaptureButton.setVisibility(View.VISIBLE);
        mode_active = true;

    }

    private void setupAutomaticMode() {
        mManualCaptureButton.setVisibility(View.VISIBLE);
        String delay = time_delay.getSelectedItem().toString();
        String units = time_units.getSelectedItem().toString();
        Long time_delay;
        TimeUnit time_units_value;
        if (units == getString(R.string.minutes)) {
            time_delay = Long.parseLong(delay) * (60); //Convert minutes to seconds
            time_units_value = TimeUnit.SECONDS;
        } else {
            //in Seconds
            time_delay = Long.parseLong(delay);
            time_units_value = TimeUnit.SECONDS;
        }

        setGeoTraceScheuler(time_delay, time_units_value);
        mode_active = true;
    }

    private void addLocationMarker() {
        Marker marker = new Marker(mapView);
        marker.setPosition(mMyLocationOverlay.getMyLocation());
        Float last_know_acuracy =
                mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
        mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
        marker.setIcon(getResources().getDrawable(R.drawable.ic_place_black_36dp));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setDraggable(true);
        marker.setOnMarkerDragListener(draglistner);
        marker.setSubDescription(Float.toString(last_know_acuracy));
        map_markers.add(marker);

        marker.setOnMarkerClickListener(nullmarkerlistner);
        mapView.getOverlays().add(marker);
        pathOverlay.addPoint(marker.getPosition());
        mapView.invalidate();
    }

    private void saveGeoTrace() {
        returnLocation();
        finish();
    }

    private void showPolyonErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.polygon_validator))
                .setPositiveButton(getString(R.string.dialog_continue),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        }).show();

    }


    private String generateReturnString() {
        String temp_string = "";
        for (int i = 0; i < map_markers.size(); i++) {
            String lat = Double.toString(map_markers.get(i).getPosition().getLatitude());
            String lng = Double.toString(map_markers.get(i).getPosition().getLongitude());
            String alt = Integer.toString(map_markers.get(i).getPosition().getAltitude());
            String acu = map_markers.get(i).getSubDescription();
            temp_string = temp_string + lat + " " + lng + " " + alt + " " + acu + ";";
        }
        return temp_string;
    }

    private void returnLocation() {
        final_return_string = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOTRACE_RESULTS,
                final_return_string);
        setResult(RESULT_OK, i);
        finish();
    }

    private OnMarkerClickListener nullmarkerlistner = new Marker.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    private void createPolygon() {
        map_markers.add(map_markers.get(0));
        pathOverlay.addPoint(map_markers.get(0).getPosition());
        mapView.invalidate();
    }

    private void update_polygon() {
        pathOverlay.clearPath();
        for (int i = 0; i < map_markers.size(); i++) {
            pathOverlay.addPoint(map_markers.get(i).getPosition());
        }
        mapView.invalidate();
    }


    private OnMarkerDragListener draglistner = new Marker.OnMarkerDragListener() {
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
        map_markers.clear();
        pathOverlay.clearPath();
        mapView.getOverlays().clear();
        mClearButton.setEnabled(false);
        overlayMyLocationLayers();
        overlayMapLayerListner();
        mapView.invalidate();
        mPlayButton.setEnabled(true);
        mode_active = false;
    }

    private void zoomtoBounds() {
        mapView.getController().setZoom(4);
        mapView.invalidate();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                int minLat = Integer.MAX_VALUE;
                int maxLat = Integer.MIN_VALUE;
                int minLong = Integer.MAX_VALUE;
                int maxLong = Integer.MIN_VALUE;
                Integer size = map_markers.size();
                for (int i = 0; i < size; i++) {
                    GeoPoint temp_marker = map_markers.get(i).getPosition();
                    if (temp_marker.getLatitudeE6() < minLat) {
                        minLat = temp_marker.getLatitudeE6();
                    }
                    if (temp_marker.getLatitudeE6() > maxLat) {
                        maxLat = temp_marker.getLatitudeE6();
                    }
                    if (temp_marker.getLongitudeE6() < minLong) {
                        minLong = temp_marker.getLongitudeE6();
                    }
                    if (temp_marker.getLongitudeE6() > maxLong) {
                        maxLong = temp_marker.getLongitudeE6();
                    }
                }
                BoundingBoxE6 boundingBox = new BoundingBoxE6(maxLat, maxLong, minLat, minLong);
                mapView.zoomToBoundingBox(boundingBox);
                mapView.invalidate();
            }
        }, 100);
        mapView.invalidate();

    }


    public void showZoomDialog() {
        if (zoomDialog == null) {
            AlertDialog.Builder p_builder = new AlertDialog.Builder(this);
            p_builder.setTitle(getString(R.string.zoom_to_where));
            p_builder.setView(zoomDialogView)
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
            zoomDialog = p_builder.create();
        }

        if (mMyLocationOverlay.getMyLocation() != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }
        //If feature enable zoom to button else disable
        if (map_markers.size() != 0) {
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

    @Override
    public void onLocationChanged(Location location) {
        if (mode_active) {
            mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}