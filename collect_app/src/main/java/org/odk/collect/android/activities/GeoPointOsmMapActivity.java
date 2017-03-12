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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.InfoLogger;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoPointWidget;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Version of the GeoPointMapActivity that uses the new OSMDDroid
 *
 * @author jonnordling@gmail.com
 */
public class GeoPointOsmMapActivity extends FragmentActivity implements LocationListener,
        OnMarkerDragListener, MapEventsReceiver, IRegisterReceiver {

    private SharedPreferences sharedPreferences;
    private String basemap;

	private static final String LOCATION_COUNT = "locationCount";

    //private GoogleMap mMap;
    private MapView mMap;

    private Handler handler = new Handler();
    private Marker mMarker;

    private GeoPoint mLatLng;

    private TextView mLocationStatus;
    private TextView mlocationInfo;

    private LocationManager mLocationManager;
    private MapEventsOverlay overlayEventos;

    private Location mLocation;
    private Button mSaveLocationButton;
    private Button mReloadLocationButton;
    private Button mLayersButton;

    private boolean mCaptureLocation = false;
    private boolean setClear = false;
    private boolean mIsDragged = false;
    private Button mShowLocationButton;

    private boolean mGPSOn = false;
    private boolean mNetworkOn = false;

    private double mLocationAccuracy;
    private int mLocationCount = 0;

    private boolean mZoomed = false;
    private MapHelper mHelper;

    private AlertDialog zoomDialog;
    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    private Button clearPointButton;

    public MyLocationNewOverlay mMyLocationOverlay;

    private Boolean read_only = false;
    private Boolean draggable = false;
    private Boolean intent_draggable = false;
    private Boolean location_from_intent = false;
    private int location_count_num = 0;
    private int location_count_found_limit = 1;
    private Boolean foundFirstLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState != null) {
            mLocationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }

        try {
            setContentView(R.layout.geopoint_osm_layout);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        if (PlayServicesUtil.checkPlayServices(GeoPointOsmMapActivity.this)) {

            mMap = (MapView) findViewById(R.id.omap);
            mHelper = new MapHelper(this, mMap, GeoPointOsmMapActivity.this);
            mMap.setMultiTouchControls(true);
            mMap.setBuiltInZoomControls(true);
            mMarker = new Marker(mMap);
            Context c = getApplicationContext();
            mMarker.setIcon(ContextCompat.getDrawable(c,R.drawable.ic_place_black_36dp));
            mMyLocationOverlay = new MyLocationNewOverlay(this, mMap);

            handler.postDelayed(new Runnable() {
                public void run() {
                    GeoPoint point = new GeoPoint(34.08145, -39.85007);
                    mMap.getController().setZoom(4);
                    mMap.getController().setCenter(point);
                }
            }, 100);

            mLocationAccuracy = GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
            mLocationStatus = (TextView) findViewById(R.id.location_status);
            mlocationInfo = (TextView) findViewById(R.id.location_info);


            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            mSaveLocationButton = (Button) findViewById(R.id.accept_location);
            mSaveLocationButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                            "OK");
                    returnLocation();
                }
            });

            mReloadLocationButton = (Button) findViewById(R.id.reload_location);
            mReloadLocationButton.setEnabled(false);
            mReloadLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.getOverlays().add(mMarker);
                    setClear = false;
                    mLatLng = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
                    mMarker.setPosition(mLatLng);
                    mCaptureLocation = true;
                    mIsDragged = false;
                    zoomToPoint();
                }

            });

            // Focuses on marked location
            mShowLocationButton = ((Button) findViewById(R.id.show_location));
            mShowLocationButton.setVisibility(View.VISIBLE);
            mShowLocationButton.setEnabled(false);
            mShowLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger()
                            .logInstanceAction(this, "showLocation", "onClick");
                    showZoomDialog();
                }
            });

            // not clickable until we have a marker set....
            mShowLocationButton.setClickable(false);

            // Menu Layer Toggle
            mLayersButton = ((Button) findViewById(R.id.layer_menu));
            mLayersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHelper.showLayersDialog(GeoPointOsmMapActivity.this);

                }
            });


            zoomDialogView = getLayoutInflater().inflate(R.layout.geopoint_zoom_dialog, null);

            zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
            zoomLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomToLocation();
                    mMap.invalidate();
                    zoomDialog.dismiss();
                }
            });

            zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_point);
            zoomPointButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    zoomToPoint();
                    mMap.invalidate();
                    zoomDialog.dismiss();
                }
            });

            clearPointButton = (Button) findViewById(R.id.clear);
            clearPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.getOverlays().remove(mMarker);
                    mMarker.remove(mMap);
                    if (mLocation != null) {
                        mReloadLocationButton.setEnabled(true);
//					mLocationStatus.setVisibility(View.VISIBLE);
                    }
                    mLocationStatus.setVisibility(View.VISIBLE);
                    mMap.getOverlays().remove(mMarker);
                    mMarker.remove(mMap);
                    setClear = true;
                    mIsDragged = false;
                    mCaptureLocation = false;
                    draggable = intent_draggable;
                    location_from_intent = false;
                    overlayMyLocationLayers();
                    mMap.invalidate();
                }
            });

            Intent intent = getIntent();
            if (intent != null && intent.getExtras() != null) {

                if (intent.hasExtra(GeoPointWidget.DRAGGABLE_ONLY)) {
                    draggable = intent.getBooleanExtra(GeoPointWidget.DRAGGABLE_ONLY, false);
                    intent_draggable = draggable;
                    if (!intent_draggable) {
                        // Not Draggable, set text for Map else leave as placement-map text
                        mlocationInfo.setText(getString(R.string.geopoint_no_draggable_instruction));
                    }
                }

                if (intent.hasExtra(GeoPointWidget.READ_ONLY)) {
                    read_only = intent.getBooleanExtra(GeoPointWidget.READ_ONLY, false);
                    if (read_only) {
                        mCaptureLocation = true;
                        clearPointButton.setEnabled(false);
                    }
                }

                if (intent.hasExtra(GeoPointWidget.LOCATION)) {
                    double[] location = intent.getDoubleArrayExtra(GeoPointWidget.LOCATION);
                    mLatLng = new GeoPoint(location[0], location[1]);
                    mReloadLocationButton.setEnabled(false);
                    mCaptureLocation = true;
                    mIsDragged = true;
                    draggable = false; // If data loaded, must clear first
                    location_from_intent = true;


                }
                if (intent.hasExtra(GeoPointWidget.ACCURACY_THRESHOLD)) {
                    mLocationAccuracy = intent.getDoubleExtra(GeoPointWidget.ACCURACY_THRESHOLD,
                            GeoPointWidget.DEFAULT_LOCATION_ACCURACY);
                }

            }

            if (mLatLng != null) {
                mMarker.setPosition(mLatLng);
                mMap.getOverlays().add(mMarker);
                mMap.invalidate();
                mCaptureLocation = true;
                foundFirstLocation = true;
                mZoomed = true;
                zoomToPoint();
            }
        } else {
            PlayServicesUtil.requestPlayServicesErrorDialog(GeoPointOsmMapActivity.this);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, mLocationCount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mHelper.setBasemap();
        }
        if (mLocationManager != null) {
            upMyLocationOverlayLayers();
        }
    }

    private void upMyLocationOverlayLayers() {
        // make sure we have a good location provider before continuing
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            }
        }

        mShowLocationButton.setClickable(mMarker != null);

        if (!mGPSOn && !mNetworkOn) {
            showGPSDisabledAlertToUser();
        } else {
            overlayMyLocationLayers();
        }
    }

    private void overlayMyLocationLayers() {
        mMap.getOverlays().add(mMyLocationOverlay);
        if (draggable & !read_only) {
            if (mMarker != null) {
                mMarker.setOnMarkerDragListener(this);
                mMarker.setDraggable(true);
            }
            overlayEventos = new MapEventsOverlay(getBaseContext(), this);
            mMap.getOverlays().add(overlayEventos);
        }

        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void zoomToPoint() {
        if (mLatLng != null) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    mMap.getController().setZoom(16);
                    mMap.getController().setCenter(mLatLng);
                    mMap.invalidate();
                }
            }, 200);
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

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */

    private void returnLocation() {
        Intent i = new Intent();
        if (setClear || (read_only && mLatLng == null)) {
            i.putExtra(FormEntryActivity.LOCATION_RESULT, "");
            setResult(RESULT_OK, i);

        } else if (mIsDragged || read_only || location_from_intent) {
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    mLatLng.getLatitude() + " " + mLatLng.getLongitude() + " "
                            + 0 + " " + 0);
            setResult(RESULT_OK, i);
        } else if (mLocation != null) {
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    mLocation.getLatitude() + " " + mLocation.getLongitude() + " "
                            + mLocation.getAltitude() + " " + mLocation.getAccuracy());
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


    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;
        if (setClear) {
            mReloadLocationButton.setEnabled(true);
        }
        if (mLocation != null) {
            if (location_count_num >= location_count_found_limit) {
                mShowLocationButton.setEnabled(true);
                if (!mCaptureLocation & !setClear) {
                    mLatLng = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
                    mMap.getOverlays().add(mMarker);
                    mMarker.setPosition(mLatLng);
                    mCaptureLocation = true;
                    mReloadLocationButton.setEnabled(true);
                }
                if (!foundFirstLocation) {
//                        zoomToPoint();
                    showZoomDialog();
                    foundFirstLocation = true;
                }
                mLocationStatus.setText(
                        getString(R.string.location_provider_accuracy, mLocation.getProvider(),
                                truncateFloat(mLocation.getAccuracy())));
            } else {
                // Prevent from forever increasing
                if (location_count_num <= 100) {
                    location_count_num++;
                }
            }


//				if (mLocation.getLatitude() != mMarker.getPosition().getLatitude() & mLocation
// .getLongitude() != mMarker.getPosition().getLongitude()) {
//					mReloadLocationButton.setEnabled(true);
//				}
//
//				//If location is accurate enough, stop updating position and make the marker
// draggable
//				if (mLocation.getAccuracy() <= mLocationAccuracy) {
//					stopGeolocating();
//				}

        } else {
            InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
                    " onLocationChanged(" + mLocationCount + ") null location");
        }
    }


    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMarkerDrag(Marker arg0) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mLatLng = marker.getPosition();
        mIsDragged = true;
        mCaptureLocation = true;
        setClear = false;
        mMap.getController().animateTo(mLatLng);
        mMap.getController().setZoom(mMap.getZoomLevel());
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
//		stopGeolocating();
    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        if (mMarker == null) {
            mMarker = new Marker(mMap);

        }
        mShowLocationButton.setEnabled(true);
        mMap.invalidate();
        mMarker.setPosition(geoPoint);
        Context c = getApplicationContext();
        mMarker.setIcon(ContextCompat.getDrawable(c,R.drawable.ic_place_black_36dp));
        mMarker.setDraggable(true);
        mLatLng = geoPoint;
        mIsDragged = true;
        setClear = false;
        mCaptureLocation = true;
        mMap.getOverlays().add(mMarker);
        return false;
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
        //If feature enable zoom to button else disable
        if (mMyLocationOverlay.getMyLocation() != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }

        if (mLatLng != null & !setClear) {
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

    private void zoomToLocation() {
        if (mMyLocationOverlay.getMyLocation() != null) {
            final GeoPoint location = new GeoPoint(mLocation.getLatitude(),
                    mLocation.getLongitude());
            handler.postDelayed(new Runnable() {
                public void run() {
                    mMap.getController().setZoom(16);
                    mMap.getController().setCenter(location);
                    mMap.invalidate();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PlayServicesUtil.PLAY_SERVICE_ERROR_REQUEST_CODE) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
