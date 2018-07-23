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
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import org.odk.collect.android.R;
import org.odk.collect.android.fragments.OsmMapFragment;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoShapeWidget;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfLocationPermissionsGranted;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 */


public class GeoShapeOsmMapActivity extends CollectAbstractActivity implements IRegisterReceiver {
    private MapView map;
    private final ArrayList<Marker> mapMarkers = new ArrayList<Marker>();
    private Polyline polyline;
    public int zoomLevel = 3;
    public static final int STROKE_WIDTH = 5;
    public String finalReturnString;
    private MapEventsOverlay overlayEvents;
    private boolean clearButtonTest;
    private ImageButton clearButton;
    public boolean gpsStatus = true;
    private ImageButton locationButton;
    public MyLocationNewOverlay myLocationOverlay;
    public boolean dataLoaded;

    private MapHelper helper;

    private AlertDialog zoomDialog;
    private View zoomDialogView;

    private Button zoomPointButton;
    private Button zoomLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIfLocationPermissionsGranted(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(getString(R.string.geoshape_title));
        setContentView(R.layout.geoshape_layout);
        OsmMapFragment mapFragment = new OsmMapFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(this::setupMap);
    }

    private void setupMap(MapView map) {
        this.map = map;
        helper = new MapHelper(this, map, this);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        map.setTilesScaledToDpi(true);
        map.setMapListener(mapViewListener);
        overlayPointPathListener();
        ImageButton saveButton = findViewById(R.id.save);
        clearButton = findViewById(R.id.clear);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mapMarkers.isEmpty()) {
                    showClearDialog();
                }
            }
        });
        ImageButton layersButton = findViewById(R.id.layers);
        layersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                helper.showLayersDialog();

            }
        });
        locationButton = findViewById(R.id.gps);
        locationButton.setEnabled(false);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showZoomDialog();
            }
        });


        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        myLocationOverlay = new MyLocationNewOverlay(map);


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoShapeWidget.SHAPE_LOCATION)) {
                clearButton.setEnabled(true);
                dataLoaded = true;
                String s = intent.getStringExtra(GeoShapeWidget.SHAPE_LOCATION);
                overlayIntentPolygon(s);
                //zoomToCentroid();
                locationButton.setEnabled(true);
                zoomToBounds();
            }
        } else {
            myLocationOverlay.runOnFirstFix(centerAroundFix);
            clearButton.setEnabled(false);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    GeoPoint point = new GeoPoint(34.08145, -39.85007);
                    map.getController().setZoom(3);
                    map.getController().setCenter(point);
                }
            }, 100);

        }

        map.invalidate();

        zoomDialogView = getLayoutInflater().inflate(R.layout.geo_zoom_dialog, null);

        zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zoomToMyLocation();
                map.invalidate();
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = zoomDialogView.findViewById(R.id.zoom_saved_location);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //zoomToCentroid();
                zoomToBounds();
                map.invalidate();
                zoomDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            helper.setBasemap();
        }

        upMyLocationOverlayLayers();
    }

    @Override
    public void onBackPressed() {
        if (!mapMarkers.isEmpty()) {
            showBackDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        disableMyLocation();
        super.onPause();
    }

    @Override
    protected void onStop() {
        disableMyLocation();
        super.onStop();
    }


    private void overlayIntentPolygon(String str) {
        clearButton.setEnabled(true);
        clearButtonTest = true;
        String s = str.replace("; ", ";");
        String[] sa = s.split(";");
        for (int i = 0; i < (sa.length - 1); i++) {
            String[] sp = sa[i].split(" ");
            double[] gp = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(gp[0], gp[1]));
            marker.setDraggable(true);
            marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_black));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener(nullMarkerListener);
            mapMarkers.add(marker);
            // pathOverlay.addPoint(marker.getPosition());
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(dragListener);
            map.getOverlays().add(marker);
        }
        update_polygon();
        map.getOverlays().remove(overlayEvents);
    }


    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable centerAroundFix = new Runnable() {
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    locationButton.setEnabled(true);
                    showZoomDialog();
                }
            });
        }
    };

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

    private void upMyLocationOverlayLayers() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            overlayMyLocationLayers();
        } else {
            showGPSDisabledAlertToUser();
        }

    }

    private void overlayMyLocationLayers() {
        map.getOverlays().add(myLocationOverlay);
        myLocationOverlay.setEnabled(true);
        myLocationOverlay.enableMyLocation();
    }

    private void zoomToMyLocation() {
        if (myLocationOverlay.getMyLocation() != null) {
            map.getController().setZoom(15);
            map.getController().setCenter(myLocationOverlay.getMyLocation());
        }
    }

    private void disableMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            myLocationOverlay.setEnabled(false);
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
            gpsStatus = false;
        }
    }


    private void overlayPointPathListener() {
        overlayEvents = new MapEventsOverlay(receive);
        polyline = new Polyline();
        polyline.setColor(Color.RED);
        Paint paint = polyline.getPaint();
        paint.setStrokeWidth(STROKE_WIDTH);
        map.getOverlays().add(polyline);
        map.getOverlays().add(overlayEvents);
        map.invalidate();
    }

    private void clearFeatures() {
        clearButtonTest = false;
        mapMarkers.clear();
        polyline.setPoints(new ArrayList<GeoPoint>());
        map.getOverlays().clear();
        clearButton.setEnabled(false);
        //saveButton.setEnabled(false);
        overlayPointPathListener();
        overlayMyLocationLayers();
        map.invalidate();

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

    private void showBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.geo_exit_warning))
                .setPositiveButton(getString(R.string.discard),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();

    }

    private String generateReturnString() {
        String tempString = "";
        if (mapMarkers.size() > 1) {
            if (Collections.frequency(mapMarkers, mapMarkers.get(0)) < 2) {
                mapMarkers.add(mapMarkers.get(0));
            }
            for (int i = 0; i < mapMarkers.size(); i++) {
                String lat = Double.toString(mapMarkers.get(i).getPosition().getLatitude());
                String lng = Double.toString(mapMarkers.get(i).getPosition().getLongitude());
                String alt = "0.0";
                String acu = "0.0";
                tempString = tempString + lat + " " + lng + " " + alt + " " + acu + ";";
            }
        }
        return tempString;
    }

    private void returnLocation() {
        finalReturnString = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                finalReturnString);
        setResult(RESULT_OK, i);
        if (mapMarkers.size() < 4) {
            ToastUtils.showShortToastInMiddle(getString(R.string.polygon_validator));
        } else {
            finish();
        }
    }

    private void update_polygon() {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < mapMarkers.size(); i++) {
            points.add(mapMarkers.get(i).getPosition());
        }
        points.add(mapMarkers.get(0).getPosition());

        polyline.setPoints(points);
        map.invalidate();
    }

    private final MapEventsReceiver receive = new MapEventsReceiver() {
        @Override
        public boolean longPressHelper(GeoPoint point) {
            if (!clearButtonTest) {
                clearButton.setEnabled(true);
                clearButtonTest = true;
            }
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setDraggable(true);
            marker.setIcon(ContextCompat.getDrawable(GeoShapeOsmMapActivity.this, R.drawable.ic_place_black));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener(nullMarkerListener);
            mapMarkers.add(marker);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(dragListener);
            map.getOverlays().add(marker);
            List<GeoPoint> points = polyline.getPoints();
            points.add(marker.getPosition());
            polyline.setPoints(points);
            update_polygon();
            map.invalidate();
            return false;
        }

        @Override
        public boolean singleTapConfirmedHelper(GeoPoint arg0) {
            return false;
        }
    };

    private final MapListener mapViewListener = new MapListener() {
        @Override
        public boolean onZoom(ZoomEvent zoomLev) {
            zoomLevel = zoomLev.getZoomLevel();
            return false;
        }

        @Override
        public boolean onScroll(ScrollEvent arg0) {
            return false;
        }

    };

    private final Marker.OnMarkerDragListener dragListener = new Marker.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            update_polygon();

        }

        @Override
        public void onMarkerDrag(Marker marker) {
            update_polygon();

        }
    };


    private final Marker.OnMarkerClickListener nullMarkerListener = new Marker.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    /*
        This functions should be added to the mapHelper Class

     */
    private void zoomToBounds() {
        map.getController().setZoom(4);
        map.invalidate();
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
                map.zoomToBoundingBox(boundingBox, false);
                map.invalidate();
            }
        }, 100);
        map.invalidate();

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
    public void destroy() {

    }
}
