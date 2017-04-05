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
import org.osmdroid.DefaultResourceProxyImpl;

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
    private Button play_button;
    private Button save_button;
    public Button layers_button;
    public Button clear_button;
    private Button manual_button;
    private Button pause_button;
    private Button location_button;
    public AlertDialog.Builder builder;
    private View traceSettingsView;
    public LayoutInflater inflater;
    private AlertDialog alert;
    public AlertDialog.Builder p_builder;
    private View polygonPolylineView;
    private AlertDialog p_alert;
    private Boolean beenPaused = false;
    private Integer TRACE_MODE = 1; // 0 manual, 1 is automatic
    private Boolean play_check = false;
    private Spinner time_units;
    private Spinner time_delay;
    public DefaultResourceProxyImpl resource_proxy;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Boolean mGPSOn = false;
    private Boolean mNetworkOn = false;
    private Location curLocation;
    private LatLng curlatLng;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private String final_return_string;
    private ArrayList<Marker> markerArray = new ArrayList<Marker>();
    private Button polygon_save;
    private Button polyline_save;
    public Button layers;
    private MapHelper mHelper;

    private AlertDialog zoomDialog;

    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    private Boolean firstLocationFound = false;
    private Boolean mode_active = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geotrace_google_layout);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
    }

    private void setupMap(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        mHelper = new MapHelper(GeoTraceGoogleMapActivity.this, mMap);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(GeoTraceGoogleMapActivity.this);
        mMap.setOnMarkerDragListener(GeoTraceGoogleMapActivity.this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);

        clear_button = (Button) findViewById(R.id.clear);
        clear_button.setOnClickListener(new View.OnClickListener() {
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
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
        time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
        time_delay.setSelection(3);
        time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
        pause_button = (Button) findViewById(R.id.pause);
        pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                play_button.setVisibility(View.VISIBLE);
                if (markerArray != null && markerArray.size() > 0) {
                    clear_button.setEnabled(true);
                }
                pause_button.setVisibility(View.GONE);
                manual_button.setVisibility(View.GONE);
                play_check = true;
                mode_active = false;
                try {
                    schedulerHandler.cancel(true);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        });
        layers_button = (Button) findViewById(R.id.layers);


        save_button = (Button) findViewById(R.id.geotrace_save);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0) {
                    p_alert.show();
                } else {
                    saveGeoTrace();
                }
            }
        });
        play_button = (Button) findViewById(R.id.play);
        play_button.setEnabled(false);
        play_button.setOnClickListener(new View.OnClickListener() {
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
                            //Do nothing
                        }
                    }
                    play_check = true;
                } else {
                    play_check = false;
                    startGeoTrace();
                }
            }
        });

        if (markerArray == null || markerArray.size() == 0) {
            clear_button.setEnabled(false);
        }

        manual_button = (Button) findViewById(R.id.manual_button);
        manual_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationMarker();
            }
        });

        polygon_save = (Button) polygonPolylineView.findViewById(R.id.polygon_save);
        polygon_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() > 2) {
                    createPolygon();
                    p_alert.dismiss();
                    saveGeoTrace();
                } else {
                    p_alert.dismiss();
                    showPolyonErrorDialog();
                }
            }
        });
        polyline_save = (Button) polygonPolylineView.findViewById(R.id.polyline_save);
        polyline_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p_alert.dismiss();
                saveGeoTrace();
            }
        });

        buildDialogs();

        layers = (Button) findViewById(R.id.layers);
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog(GeoTraceGoogleMapActivity.this);
            }
        });

        location_button = (Button) findViewById(R.id.show_location);
        location_button.setOnClickListener(new View.OnClickListener() {
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
                zoomtoBounds();
                zoomDialog.dismiss();
            }
        });
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
                curLocation = mLocationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
            }
        }

        if (mGPSOn) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    GeoTraceGoogleMapActivity.this);
        }
        if (mNetworkOn) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                    GeoTraceGoogleMapActivity.this);
        }

        if (!mGPSOn & !mNetworkOn) {
            showGPSDisabledAlertToUser();
        }
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
                String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
                play_button.setEnabled(false);
                clear_button.setEnabled(true);
                firstLocationFound = true;
                location_button.setEnabled(true);
                overlayIntentTrace(s);
                zoomtoBounds();
            }
        } else {
            if (curLocation != null) {
                curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
            }
        }

        mHelper.setBasemap();
    }

    private void overlayIntentTrace(String str) {
        mMap.setOnMapLongClickListener(null);
        String s = str.replace("; ", ";");
        String[] sa = s.split(";");
        for (int i = 0; i < (sa.length); i++) {
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            LatLng point = new LatLng(gp[0], gp[1]);
            polylineOptions.add(point);
            MarkerOptions mMarkerOptions = new MarkerOptions().position(point).draggable(true);
            Marker marker = mMap.addMarker(mMarkerOptions);
            markerArray.add(marker);
        }
        polyline = mMap.addPolyline(polylineOptions);
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


    private void returnLocation() {

        final_return_string = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOTRACE_RESULTS,
                final_return_string);
        setResult(RESULT_OK, i);
        finish();
    }

    private void disableMyLocation() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    private String generateReturnString() {
        String temp_string = "";
        for (int i = 0; i < markerArray.size(); i++) {
            String lat = Double.toString(markerArray.get(i).getPosition().latitude);
            String lng = Double.toString(markerArray.get(i).getPosition().longitude);
            String alt = "0.0";
            String acu = "0.0";
            temp_string = temp_string + lat + " " + lng + " " + alt + " " + acu + ";";
        }
        return temp_string;
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
                        play_check = false;

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        play_check = false;

                    }
                });


        alert = builder.create();

        p_builder = new AlertDialog.Builder(this);
        p_builder.setTitle(getString(R.string.polygon_or_polyline));
        p_builder.setView(polygonPolylineView)

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

        p_alert = p_builder.create();

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

                zoomtoBounds();

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
        TRACE_MODE = idx;
        if (TRACE_MODE == 0) {
            setupManualMode();
        } else if (TRACE_MODE == 1) {
            setupAutomaticMode();
        } else {

        }
        play_button.setVisibility(View.GONE);
        pause_button.setVisibility(View.VISIBLE);


    }

    private void setupManualMode() {

        manual_button.setVisibility(View.VISIBLE);
        mode_active = true;

    }

    private void setupAutomaticMode() {
        manual_button.setVisibility(View.VISIBLE);
        String delay = time_delay.getSelectedItem().toString();
        String units = time_units.getSelectedItem().toString();
        Long time_delay;
        TimeUnit time_units_value;
        if (units == getString(R.string.minutes)) {
            time_delay = Long.parseLong(delay) * (60 * 60);
            time_units_value = TimeUnit.SECONDS;

        } else {
            //in Seconds
            time_delay = Long.parseLong(delay);
            time_units_value = TimeUnit.SECONDS;
        }

        setGeoTraceScheuler(time_delay, time_units_value);
        mode_active = true;
    }

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

    private void createPolygon() {
        markerArray.add(markerArray.get(0));
        update_polyline();
    }

	/*
        This functions handels the delay and the Runable for
	*/

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

    private void update_polyline() {
        ArrayList<LatLng> tempLat = new ArrayList<LatLng>();
        for (int i = 0; i < markerArray.size(); i++) {
            LatLng latLng = markerArray.get(i).getPosition();
            tempLat.add(latLng);
        }
        polyline.setPoints(tempLat);
    }

    private void addLocationMarker() {
        LatLng latLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions().position(latLng).draggable(true);
        Marker marker = mMap.addMarker(mMarkerOptions);
        markerArray.add(marker);
        if (polyline == null) {
            polylineOptions.add(latLng);
            polyline = mMap.addPolyline(polylineOptions);
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng, 17));
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        curLocation = location;
        if (!firstLocationFound) {
            firstLocationFound = true;
            play_button.setEnabled(true);
            showZoomDialog();

        }
        curlatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        if (mode_active) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(curlatLng));
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

    private void showPolyonErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.polygon_validator))
                .setPositiveButton(getString(R.string.dialog_continue),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).show();

    }

    private void clearFeatures() {

        mMap.clear();
        mode_active = false;
        clear_button.setEnabled(false);
        polyline = null;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        markerArray.clear();
        pause_button.setVisibility(View.GONE);
        clear_button.setEnabled(false);
        manual_button.setVisibility(View.GONE);
        play_button.setVisibility(View.VISIBLE);
        play_button.setEnabled(true);
        play_check = false;
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

    private void zoomtoBounds() {
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
                mMap.animateCamera(cu);
            }
        }, 100);

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
