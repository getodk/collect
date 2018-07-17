/*
 * Copyright (C) 2016 GeoODK
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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.GeoPointUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoPointWidget;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DecimalFormat;

import timber.log.Timber;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfLocationPermissionsGranted;

/**
 * Version of the GeoPointMapActivity that uses the new OSMDDroid
 *
 * @author jonnordling@gmail.com
 */
public class GeoPointOsmMapActivity extends CollectAbstractActivity implements LocationListener,
        Marker.OnMarkerDragListener, MapEventsReceiver, IRegisterReceiver,
        LocationClient.LocationClientListener {

    private static final String LOCATION_COUNT = "locationCount";

    //private GoogleMap map;
    private MapView map;

    private final Handler handler = new Handler();
    private Marker marker;

    private GeoPoint latLng;

    private TextView locationStatus;

    private LocationClient locationClient;

    private Location location;
    private ImageButton reloadLocationButton;

    private boolean captureLocation;
    private boolean setClear;
    private boolean isDragged;
    private ImageButton showLocationButton;

    private int locationCount;

    private MapHelper helper;

    private AlertDialog zoomDialog;
    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    public MyLocationNewOverlay myLocationOverlay;

    private boolean readOnly;
    private boolean draggable;
    private boolean intentDraggable;
    private boolean locationFromIntent;
    private int locationCountNum;
    private boolean foundFirstLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            setContentView(R.layout.geopoint_osm_layout);
        } catch (NoClassDefFoundError e) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        map = findViewById(R.id.omap);
        if (helper == null) {
            // For testing:
            helper = new MapHelper(this, map, this);

            map.setMultiTouchControls(true);
            map.setBuiltInZoomControls(true);
            map.setTilesScaledToDpi(true);
        }

        marker = new Marker(map);
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_black));
        myLocationOverlay = new MyLocationNewOverlay(map);

        handler.postDelayed(new Runnable() {
            public void run() {
                GeoPoint point = new GeoPoint(34.08145, -39.85007);
                map.getController().setZoom(4);
                map.getController().setCenter(point);
            }
        }, 100);

        locationStatus = findViewById(R.id.location_status);
        TextView locationInfo = findViewById(R.id.location_info);

        locationClient = LocationClients.clientForContext(this);
        locationClient.setListener(this);

        ImageButton saveLocationButton = findViewById(R.id.accept_location);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                        "OK");
                returnLocation();
            }
        });

        reloadLocationButton = findViewById(R.id.reload_location);
        reloadLocationButton.setEnabled(false);
        reloadLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getOverlays().add(marker);
                setClear = false;
                latLng = new GeoPoint(location.getLatitude(), location.getLongitude());
                marker.setPosition(latLng);
                captureLocation = true;
                isDragged = false;
                zoomToPoint();
            }

        });

        // Focuses on marked location
        showLocationButton = findViewById(R.id.show_location);
        showLocationButton.setVisibility(View.VISIBLE);
        showLocationButton.setEnabled(false);
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logInstanceAction(this, "showLocation", "onClick");
                showZoomDialog();
            }
        });

        // not clickable until we have a marker set....
        showLocationButton.setClickable(false);

        // Menu Layer Toggle
        ImageButton layersButton = findViewById(R.id.layer_menu);
        layersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.showLayersDialog();

            }
        });


        zoomDialogView = getLayoutInflater().inflate(R.layout.geo_zoom_dialog, null);

        zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToLocation();
                map.invalidate();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = zoomDialogView.findViewById(R.id.zoom_saved_location);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zoomToPoint();
                map.invalidate();
                zoomDialog.dismiss();
            }
        });

        ImageButton clearPointButton = findViewById(R.id.clear);
        clearPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getOverlays().remove(marker);
                marker.remove(map);
                if (location != null) {
                    reloadLocationButton.setEnabled(true);
                    //locationStatus.setVisibility(View.VISIBLE);
                }
                locationStatus.setVisibility(View.VISIBLE);
                map.getOverlays().remove(marker);
                marker.remove(map);
                setClear = true;
                isDragged = false;
                captureLocation = false;
                draggable = intentDraggable;
                locationFromIntent = false;
                overlayMyLocationLayers();
                map.invalidate();
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
                latLng = new GeoPoint(location[0], location[1]);
                reloadLocationButton.setEnabled(false);
                captureLocation = true;
                isDragged = true;
                draggable = false; // If data loaded, must clear first
                locationFromIntent = true;


            }
        }

        if (latLng != null) {
            marker.setPosition(latLng);
            map.getOverlays().add(marker);
            map.invalidate();
            captureLocation = true;
            foundFirstLocation = true;
            zoomToPoint();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, locationCount);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);

        locationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (map != null) {
            helper.setBasemap();
        }

    }

    @Override
    protected void onStop() {
        locationClient.stop();

        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }


    private void upMyLocationOverlayLayers() {
        showLocationButton.setClickable(marker != null);

        // make sure we have a good location provider before continuing
        locationClient.requestLocationUpdates(this);

        if (!locationClient.isLocationAvailable()) {
            showGPSDisabledAlertToUser();

        } else {
            overlayMyLocationLayers();
        }
    }

    private void overlayMyLocationLayers() {
        map.getOverlays().add(myLocationOverlay);
        if (draggable && !readOnly) {
            if (marker != null) {
                marker.setOnMarkerDragListener(this);
                marker.setDraggable(true);
            }

            MapEventsOverlay overlayEvents = new MapEventsOverlay(this);
            map.getOverlays().add(overlayEvents);
        }

        myLocationOverlay.setEnabled(true);
        myLocationOverlay.enableMyLocation();
    }

    private void zoomToPoint() {
        if (latLng != null) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    map.getController().setZoom(16);
                    map.getController().setCenter(latLng);
                    map.invalidate();
                }
            }, 200);
        }

    }

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    public void returnLocation() {
        Intent i = new Intent();
        if (setClear || (readOnly && latLng == null)) {
            i.putExtra(FormEntryActivity.LOCATION_RESULT, "");
            setResult(RESULT_OK, i);

        } else if (isDragged || readOnly || locationFromIntent) {
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    latLng.getLatitude() + " " + latLng.getLongitude() + " "
                            + 0 + " " + 0);
            setResult(RESULT_OK, i);
        } else if (location != null) {
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    getResultString(location));
            setResult(RESULT_OK, i);
        } else {
            i.putExtra(FormEntryActivity.LOCATION_RESULT, "");
            setResult(RESULT_OK, i);
        }
        finish();
    }

    private String truncateFloat(float f) {
        return new DecimalFormat("#.##").format(f);
    }

    public String getResultString(Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
    }

    @Override
    public void onLocationChanged(Location location) {

        this.location = location;
        if (setClear) {
            reloadLocationButton.setEnabled(true);
        }
        if (this.location != null) {
            int locationCountFoundLimit = 1;
            if (locationCountNum >= locationCountFoundLimit) {
                showLocationButton.setEnabled(true);
                if (!captureLocation & !setClear) {
                    latLng = new GeoPoint(this.location.getLatitude(), this.location.getLongitude());
                    map.getOverlays().add(marker);
                    marker.setPosition(latLng);
                    captureLocation = true;
                    reloadLocationButton.setEnabled(true);
                }
                if (!foundFirstLocation) {
                    // zoomToPoint();
                    showZoomDialog();
                    foundFirstLocation = true;
                }
                locationStatus.setText(
                        getString(R.string.location_provider_accuracy, GeoPointUtils.capitalizeGps(this.location.getProvider()),
                                truncateFloat(this.location.getAccuracy())));
            } else {
                // Prevent from forever increasing
                if (locationCountNum <= 100) {
                    locationCountNum++;
                }
            }


            //if (location.getLatitude() != marker.getPosition().getLatitude() & location
            // .getLongitude() != marker.getPosition().getLongitude()) {
            //reloadLocationButton.setEnabled(true);
            //}
            //
            //If location is accurate enough, stop updating position and make the marker
            // draggable
            //if (location.getAccuracy() <= mLocationAccuracy) {
            //stopGeolocating();
            //}

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
        map.getController().animateTo(latLng);
        map.getController().setZoom(map.getZoomLevel());
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
        //stopGeolocating();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        if (marker == null) {
            marker = new Marker(map);

        }
        showLocationButton.setEnabled(true);
        map.invalidate();
        marker.setPosition(geoPoint);
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_black));
        marker.setDraggable(true);
        latLng = geoPoint;
        isDragged = true;
        setClear = false;
        captureLocation = true;
        map.getOverlays().add(marker);
        return false;
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
        if (myLocationOverlay.getMyLocation() != null) {
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
        zoomDialog.show();
    }

    private void zoomToLocation() {
        if (myLocationOverlay.getMyLocation() != null) {
            final GeoPoint location = new GeoPoint(this.location.getLatitude(),
                    this.location.getLongitude());
            handler.postDelayed(new Runnable() {
                public void run() {
                    map.getController().setZoom(16);
                    map.getController().setCenter(location);
                    map.invalidate();
                }
            }, 200);
        }
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

    @Override
    public void destroy() {

    }

    @Override
    public void onClientStart() {
        upMyLocationOverlayLayers();
    }

    @Override
    public void onClientStartFailure() {
        showGPSDisabledAlertToUser();
    }

    @Override
    public void onClientStop() {

    }

    public AlertDialog getZoomDialog() {
        return zoomDialog;
    }

    /**
     * For testing purposes.
     *
     * @param helper The MapHelper to set.
     */
    public void setHelper(MapHelper helper) {
        this.helper = helper;
    }
}
