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
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.InfoLogger;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoPointWidget;

import java.text.DecimalFormat;
import java.util.List;

import timber.log.Timber;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author guisalmon@gmail.com
 * @author jonnordling@gmail.com
 */
public class GeoPointMapActivity extends FragmentActivity implements LocationListener,
        OnMarkerDragListener, OnMapLongClickListener {

    private static final String LOCATION_COUNT = "locationCount";

    private GoogleMap map;
    private MarkerOptions markerOptions;
    private Marker marker;
    private LatLng latLng;

    private TextView locationStatus;
    private TextView locationInfo;

    private LocationManager locationManager;

    private Location location;
    private ImageButton reloadLocation;

    private boolean isDragged = false;
    private ImageButton showLocation;
    private boolean gpsOn = false;
    private boolean networkOn = false;

    private int locationCount = 0;

    private MapHelper helper;
    //private KmlLayer kk;

    private AlertDialog zoomDialog;
    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    private boolean setClear = false;
    private boolean captureLocation = false;
    private Boolean foundFirstLocation = false;
    private int locationCountNum = 0;
    private int locationCountFoundLimit = 1;
    private Boolean readOnly = false;
    private Boolean draggable = false;
    private Boolean intentDraggable = false;
    private Boolean locationFromIntent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState != null) {
            locationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }

        try {
            setContentView(R.layout.geopoint_layout);
        } catch (NoClassDefFoundError e) {
            Timber.e(e, "Google maps not accessible due to: %s ", e.getMessage());
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
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
        Intent i = new Intent();
        if (setClear || (readOnly && latLng == null)) {
            i.putExtra(FormEntryActivity.LOCATION_RESULT, "");
            setResult(RESULT_OK, i);

        } else if (isDragged || readOnly || locationFromIntent) {
            Timber.i("IsDragged !!!");
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    latLng.latitude + " " + latLng.longitude + " "
                            + 0 + " " + 0);
            setResult(RESULT_OK, i);
        } else if (location != null) {
            Timber.i("IsNotDragged !!!");

            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    location.getLatitude() + " " + location.getLongitude() + " "
                            + location.getAltitude() + " " + location.getAccuracy());
            setResult(RESULT_OK, i);
        }
        finish();
    }


    private String truncateFloat(float f) {
        return new DecimalFormat("#.##").format(f);
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
        helper = new MapHelper(GeoPointMapActivity.this, map);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        markerOptions = new MarkerOptions();
        helper = new MapHelper(this, map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationStatus = (TextView) findViewById(R.id.location_status);
        locationInfo = (TextView) findViewById(R.id.location_info);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ImageButton acceptLocation = (ImageButton) findViewById(R.id.accept_location);

        acceptLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                        "OK");
                returnLocation();
            }
        });

        reloadLocation = (ImageButton) findViewById(R.id.reload_location);
        reloadLocation.setEnabled(false);
        reloadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker != null) {
                    marker.remove();
                }
                latLng = null;
                marker = null;
                setClear = false;
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                markerOptions.position(latLng);
                if (marker == null) {
                    marker = map.addMarker(markerOptions);
                    if (draggable && !readOnly) {
                        marker.setDraggable(true);
                    }
                }
                captureLocation = true;
                isDragged = false;
                zoomToPoint();
            }
        });

        // Focuses on marked location
        showLocation = (ImageButton) findViewById(R.id.show_location);
        //showLocation.setClickable(false);
        showLocation.setEnabled(false);
        showLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showZoomDialog();
            }
        });

        // Menu Layer Toggle
        ImageButton layers = (ImageButton) findViewById(R.id.layer_menu);
        layers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.showLayersDialog(GeoPointMapActivity.this);
            }
        });
        zoomDialogView = getLayoutInflater().inflate(R.layout.geopoint_zoom_dialog, null);
        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToLocation();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_point);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToPoint();
                zoomDialog.dismiss();
            }
        });

        ImageButton clearPointButton = (ImageButton) findViewById(R.id.clear);
        clearPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker != null) {
                    marker.remove();
                }
                if (location != null) {
                    reloadLocation.setEnabled(true);
                    // locationStatus.setVisibility(View.VISIBLE);
                }
                // reloadLocation.setEnabled(true);
                locationInfo.setVisibility(View.VISIBLE);
                locationStatus.setVisibility(View.VISIBLE);
                latLng = null;
                marker = null;
                setClear = true;
                isDragged = false;
                captureLocation = false;
                draggable = intentDraggable;
                locationFromIntent = false;
                overlayMyLocationLayers();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoPointWidget.DRAGGABLE_ONLY)) {
                draggable = intent.getBooleanExtra(GeoPointWidget.DRAGGABLE_ONLY, false);
                intentDraggable = draggable;
                if (!intentDraggable) {
                    // Not Draggable, set text for Map else leave as placement-map text
                    locationInfo.setText(getString(R.string.geopoint_no_draggable_instruction));
                }
            }

            if (intent.hasExtra(GeoPointWidget.READ_ONLY)) {
                readOnly = intent.getBooleanExtra(GeoPointWidget.READ_ONLY, false);
                if (readOnly) {
                    captureLocation = true;
                    clearPointButton.setEnabled(false);
                }
            }

            if (intent.hasExtra(GeoPointWidget.LOCATION)) {
                double[] location = intent.getDoubleArrayExtra(GeoPointWidget.LOCATION);
                latLng = new LatLng(location[0], location[1]);
                captureLocation = true;
                reloadLocation.setEnabled(false);
                draggable = false; // If data loaded, must clear first
                locationFromIntent = true;

            }
        }
        /*Zoom only if there's a previous location*/
        if (latLng != null) {
            locationInfo.setVisibility(View.GONE);
            locationStatus.setVisibility(View.GONE);
            showLocation.setEnabled(true);
            markerOptions.position(latLng);
            marker = map.addMarker(markerOptions);
            captureLocation = true;
            foundFirstLocation = true;
            zoomToPoint();
        }

        helper.setBasemap();
        upMyLocationOverlayLayers();
    }

    private void upMyLocationOverlayLayers() {
        // make sure we have a good location provider before continuing
        locationCountNum = 0;
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gpsOn = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationCountFoundLimit = 0;
            } else if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                // Only if GPS Provider is not available use network location. bug (well know
                // android bug) http://stackoverflow
                // .com/questions/6719207/locationmanager-returns-old-cached-wifi-location-with
                // -current-timestamp
                networkOn = true;
                locationCountFoundLimit =
                        1; // increase count due to network location bug (well know android bug)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            }
        }
        //showLocation.setClickable(marker != null);
        if (!gpsOn && !networkOn) {
            showGPSDisabledAlertToUser();
        } else {
            overlayMyLocationLayers();
        }
    }

    private void overlayMyLocationLayers() {
        if (draggable & !readOnly) {
            map.setOnMarkerDragListener(this);
            map.setOnMapLongClickListener(this);
            if (marker != null) {
                marker.setDraggable(true);
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (setClear) {
            reloadLocation.setEnabled(true);
        }
        if (this.location != null) {

            if (locationCountNum >= locationCountFoundLimit) {
                showLocation.setEnabled(true);
                if (!captureLocation & !setClear) {
                    latLng = new LatLng(this.location.getLatitude(), this.location.getLongitude());
                    markerOptions.position(latLng);
                    marker = map.addMarker(markerOptions);
                    captureLocation = true;
                    reloadLocation.setEnabled(true);
                }
                if (!foundFirstLocation) {
                    //zoomToPoint();
                    showZoomDialog();
                    foundFirstLocation = true;
                }
                locationStatus.setText(
                        getString(R.string.location_provider_accuracy, this.location.getProvider(),
                                truncateFloat(this.location.getAccuracy())));
            } else {
                // Prevent from forever increasing
                if (locationCountNum <= 100) {
                    locationCountNum++;
                }
            }
        } else {
            InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis()
                    + " onLocationChanged(" + locationCount + ") null location");
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
    public void onMarkerDrag(Marker arg0) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latLng = marker.getPosition();
        isDragged = true;
        captureLocation = true;
        setClear = false;
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, map.getCameraPosition().zoom));

    }

    @Override
    public void onMarkerDragStart(Marker arg0) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        this.latLng = latLng;
        if (marker == null) {
            markerOptions.position(latLng);
            marker = map.addMarker(markerOptions);
        } else {
            marker.setPosition(latLng);
        }
        showLocation.setEnabled(true);
        marker.setDraggable(true);
        isDragged = true;
        setClear = false;
        captureLocation = true;
    }

    private void zoomToLocation() {
        LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 16));
        }
    }

    private void zoomToPoint() {
        if (latLng != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
        }

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
        //If feature enable zoom to button else disable
        if (location != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }

        if (latLng != null & !setClear) {
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