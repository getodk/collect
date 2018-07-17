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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.GeoPointUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoPointWidget;

import java.text.DecimalFormat;

import timber.log.Timber;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfLocationPermissionsGranted;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author guisalmon@gmail.com
 * @author jonnordling@gmail.com
 */
public class GeoPointMapActivity extends CollectAbstractActivity implements OnMarkerDragListener, OnMapLongClickListener,
        LocationClient.LocationClientListener, LocationListener {

    private static final String LOCATION_COUNT = "locationCount";

    private GoogleMap map;
    private MarkerOptions markerOptions;
    private Marker marker;
    private LatLng latLng;

    private TextView locationStatus;
    private TextView locationInfo;

    private LocationClient locationClient;

    private Location location;
    private ImageButton reloadLocation;

    private boolean isDragged;
    private ImageButton showLocation;

    private int locationCount;

    private MapHelper helper;
    //private KmlLayer kk;

    private AlertDialog errorDialog;

    private AlertDialog zoomDialog;
    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    private boolean setClear;
    private boolean captureLocation;
    private boolean foundFirstLocation;
    private boolean readOnly;
    private boolean draggable;
    private boolean intentDraggable;
    private boolean locationFromIntent;

    private boolean isMapReady;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIfLocationPermissionsGranted(this)) {
            finish();
            return;
        }

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

        locationStatus = findViewById(R.id.location_status);
        locationInfo = findViewById(R.id.location_info);
        reloadLocation = findViewById(R.id.reload_location);
        showLocation = findViewById(R.id.show_location);

        locationClient = LocationClients.clientForContext(this);

        isMapReady = false;
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(googleMap -> {
            setupMap(googleMap);
            locationClient.setListener(this);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);

        locationClient.start();
    }

    @Override
    protected void onStop() {
        locationClient.stop();

        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    public void returnLocation() {
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
                    getResultString(location)
            );
            setResult(RESULT_OK, i);
        }
        finish();
    }


    public String getResultString(Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
    }

    private String truncateFloat(float f) {
        return new DecimalFormat("#.##").format(f);
    }

    private void setupMap(GoogleMap googleMap) {
        map = googleMap;
        if (map == null) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }
        helper = new MapHelper(this, map);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        markerOptions = new MarkerOptions();
        helper = new MapHelper(this, map);


        ImageButton acceptLocation = findViewById(R.id.accept_location);

        acceptLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                        "OK");
                returnLocation();
            }
        });

        reloadLocation.setEnabled(false);
        reloadLocation.setOnClickListener(v -> {
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
        });

        // Focuses on marked location
        //showLocation.setClickable(false);
        showLocation.setEnabled(false);
        showLocation.setOnClickListener(v -> showZoomDialog());

        // Menu Layer Toggle
        ImageButton layers = findViewById(R.id.layer_menu);
        layers.setOnClickListener(v -> helper.showLayersDialog());
        zoomDialogView = getLayoutInflater().inflate(R.layout.geo_zoom_dialog, null);
        zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(v -> {
            zoomToLocation();
            zoomDialog.dismiss();
        });

        zoomPointButton = zoomDialogView.findViewById(R.id.zoom_saved_location);
        zoomPointButton.setOnClickListener(v -> {
            zoomToPoint();
            zoomDialog.dismiss();
        });

        ImageButton clearPointButton = findViewById(R.id.clear);
        clearPointButton.setOnClickListener(v -> {
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

        isMapReady = true;
        upMyLocationOverlayLayers();
    }

    private void upMyLocationOverlayLayers() {
        if (!locationClient.isMonitoringLocation() || !isMapReady) {
            return;
        }

        // Make sure we can access Location:
        if (!locationClient.isLocationAvailable()) {
            showGPSDisabledAlertToUser();

        } else {
            overlayMyLocationLayers();
        }
    }

    private void overlayMyLocationLayers() {
        if (draggable && !readOnly) {
            map.setOnMarkerDragListener(this);
            map.setOnMapLongClickListener(this);

            if (marker != null) {
                marker.setDraggable(true);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (setClear) {
            reloadLocation.setEnabled(true);
        }

        Location previousLocation = this.location;
        this.location = location;

        if (location != null) {
            Timber.i("onLocationChanged(%d) location: %s", locationCount, location);

            if (previousLocation != null) {
                enableShowLocation(true);

                if (!captureLocation && !setClear) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
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

                String locationString = getAccuracyStringForLocation(location);
                locationStatus.setText(locationString);
            }

        } else {
            Timber.i("onLocationChanged(%d) null location", locationCount);
        }
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
        enableShowLocation(true);
        marker.setDraggable(true);
        isDragged = true;
        setClear = false;
        captureLocation = true;
    }

    private void enableShowLocation(boolean shouldEnable) {
        if (showLocation != null) {
            showLocation.setEnabled(shouldEnable);
        }
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
                    .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
                    .setOnCancelListener(dialog -> {
                        dialog.cancel();
                        zoomDialog.dismiss();
                    });
            zoomDialog = builder.create();
        }
        //If feature enable zoom to button else disable
        if (zoomLocationButton != null) {
            if (location != null) {
                zoomLocationButton.setEnabled(true);
                zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
                zoomLocationButton.setTextColor(themeUtils.getPrimaryTextColor());
            } else {
                zoomLocationButton.setEnabled(false);
                zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
                zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
            }

            if (latLng != null & !setClear) {
                zoomPointButton.setEnabled(true);
                zoomPointButton.setBackgroundColor(Color.parseColor("#50cccccc"));
                zoomPointButton.setTextColor(themeUtils.getPrimaryTextColor());
            } else {
                zoomPointButton.setEnabled(false);
                zoomPointButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
                zoomPointButton.setTextColor(Color.parseColor("#FF979797"));
            }
        }

        zoomDialog.show();
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_gps),
                        (dialog, id) -> {
                            startActivityForResult(
                                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            errorDialog = null;
                        });

        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                (dialog, id) -> {
                    dialog.cancel();
                    errorDialog = null;
                });

        errorDialog = alertDialogBuilder.create();
        errorDialog.show();
    }

    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);
        upMyLocationOverlayLayers();
    }

    @Override
    public void onClientStartFailure() {

    }

    @Override
    public void onClientStop() {

    }

    /**
     * For testing purposes only.
     *
     * @param mapReady Whether or not the Google Map is ready.
     */
    public void setMapReady(boolean mapReady) {
        isMapReady = mapReady;
    }

    public void setCaptureLocation(boolean captureLocation) {
        this.captureLocation = captureLocation;
    }

    public AlertDialog getErrorDialog() {
        return errorDialog;
    }

    public String getLocationStatus() {
        return locationStatus.getText().toString();
    }

    public String getAccuracyStringForLocation(Location location) {
        return getString(R.string.location_provider_accuracy, GeoPointUtils.capitalizeGps(location.getProvider()),
                truncateFloat(location.getAccuracy()));
    }

    public AlertDialog getZoomDialog() {
        return zoomDialog;
    }

}