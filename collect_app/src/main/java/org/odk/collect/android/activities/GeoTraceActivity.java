/*
 * Copyright (C) 2018 Nafundi
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
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.map.GoogleMapFragment;
import org.odk.collect.android.map.MapFragment;
import org.odk.collect.android.map.MapPoint;
import org.odk.collect.android.map.OsmMapFragment;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoTraceWidget;
import org.osmdroid.tileprovider.IRegisterReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfLocationPermissionsGranted;

public class GeoTraceActivity extends CollectAbstractActivity implements IRegisterReceiver {
    public static final String PREF_VALUE_GOOGLE_MAPS = "google_maps";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture schedulerHandler;

    private MapFragment map;
    private MapHelper helper;
    private int featureId = -1;  // will be a positive featureId once map is ready
    private String originalValue = "";

    private ImageButton zoomButton;
    private ImageButton playButton;
    private ImageButton clearButton;
    private Button manualButton;
    private ImageButton pauseButton;

    private View traceSettingsView;
    private View polygonOrPolylineView;

    private AlertDialog traceSettingsDialog;
    private AlertDialog polygonOrPolylineDialog;

    private boolean beenPaused;
    private boolean modeActive;
    private Integer traceMode = 1; // 0 manual, 1 is automatic
    private boolean playCheck;
    private Spinner timeUnits;
    private Spinner timeDelay;

    private AlertDialog zoomDialog;
    private View zoomDialogView;
    private Button zoomPointButton;
    private Button zoomLocationButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIfLocationPermissionsGranted(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(getString(R.string.geotrace_title));
        setContentView(R.layout.geotrace_layout);
        createMapFragment().addTo(this, R.id.map_container, this::initMap);
    }

    public MapFragment createMapFragment() {
        String mapSdk = getIntent().getStringExtra(PreferenceKeys.KEY_MAP_SDK);
        return (mapSdk == null || mapSdk.equals(PREF_VALUE_GOOGLE_MAPS)) ?
            new GoogleMapFragment() : new OsmMapFragment();
    }

    @Override protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
        if (map != null) {
            map.setGpsLocationEnabled(true);
        }
    }

    @Override protected void onStop() {
        map.setGpsLocationEnabled(false);
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (schedulerHandler != null && !schedulerHandler.isCancelled()) {
            schedulerHandler.cancel(true);
        }
        super.onDestroy();
    }

    @Override public void destroy() { }

    public void initMap(MapFragment newMapFragment) {
        if (newMapFragment == null) {
            finish();
            return;
        }

        map = newMapFragment;
        if (map instanceof GoogleMapFragment) {
            helper = new MapHelper(this, ((GoogleMapFragment) map).getGoogleMap());
        } else if (map instanceof OsmMapFragment) {
            helper = new MapHelper(this, ((OsmMapFragment) map).getMapView(), this);
        }
        helper.setBasemap();

        traceSettingsView = getLayoutInflater().inflate(R.layout.geotrace_dialog, null);
        polygonOrPolylineView = getLayoutInflater().inflate(R.layout.polygon_polyline_dialog, null);
        timeDelay = traceSettingsView.findViewById(R.id.trace_delay);
        timeDelay.setSelection(3);
        timeUnits = traceSettingsView.findViewById(R.id.trace_scale);

        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(v -> showClearDialog());

        pauseButton = findViewById(R.id.pause);
        pauseButton.setOnClickListener(v -> {
            playButton.setVisibility(View.VISIBLE);
            if (!map.getPointsOfPoly(featureId).isEmpty()) {
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
        });

        ImageButton saveButton = findViewById(R.id.geotrace_save);
        saveButton.setOnClickListener(v -> {
            if (!map.getPointsOfPoly(featureId).isEmpty()) {
                polygonOrPolylineDialog.show();
            } else {
                finishWithResult();
            }
        });

        playButton = findViewById(R.id.play);
        playButton.setEnabled(false);
        playButton.setOnClickListener(v -> {
            if (!playCheck) {
                if (!beenPaused) {
                    traceSettingsDialog.show();
                } else {
                    RadioGroup rb = traceSettingsView.findViewById(R.id.radio_group);
                    View radioButton = rb.findViewById(rb.getCheckedRadioButtonId());
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
        });

        manualButton = findViewById(R.id.manual_button);
        manualButton.setOnClickListener(v -> addVertex());

        Button polygonSave = polygonOrPolylineView.findViewById(R.id.polygon_save);
        polygonSave.setOnClickListener(v -> {
            if (map.getPointsOfPoly(featureId).size() > 2) {
                // Close the polygon.
                map.appendPointToPoly(featureId, map.getPointsOfPoly(featureId).get(0));
                polygonOrPolylineDialog.dismiss();
                finishWithResult();
            } else {
                polygonOrPolylineDialog.dismiss();
                ToastUtils.showShortToastInMiddle(getString(R.string.polygon_validator));
            }
        });

        Button polylineSave = polygonOrPolylineView.findViewById(R.id.polyline_save);
        polylineSave.setOnClickListener(v -> {
            if (map.getPointsOfPoly(featureId).size() > 1) {
                polygonOrPolylineDialog.dismiss();
                finishWithResult();
            } else {
                polygonOrPolylineDialog.dismiss();
                ToastUtils.showShortToastInMiddle(getString(R.string.polyline_validator));
            }
        });

        buildDialogs();

        findViewById(R.id.layers).setOnClickListener(v -> helper.showLayersDialog());

        zoomButton = findViewById(R.id.zoom);
        zoomButton.setOnClickListener(v -> {
            playCheck = false;
            showZoomDialog();
        });

        List<MapPoint> points = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
            originalValue = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
            points = parsePoints(originalValue);
        }
        featureId = map.addDraggablePoly(points, false);
        zoomButton.setEnabled(!points.isEmpty());
        clearButton.setEnabled(!points.isEmpty());

        map.setGpsLocationEnabled(true);
        map.setGpsLocationListener(this::onGpsLocation);
        if (!points.isEmpty()) {
            map.zoomToBoundingBox(points, 0.6);
        } else {
            map.runOnGpsLocationReady(this::onGpsLocationReady);
        }
    }

    private void finishWithResult() {
        List<MapPoint> points = map.getPointsOfPoly(featureId);
        setResult(RESULT_OK, new Intent().putExtra(
            FormEntryActivity.GEOTRACE_RESULTS, formatPoints(points)));
        finish();
    }

    /**
     * Parses a form result string, as previously formatted by formatPoints,
     * into a list of polyline vertices.
     */
    private List<MapPoint> parsePoints(String coords) {
        List<MapPoint> points = new ArrayList<>();
        for (String vertex : (coords == null ? "" : coords).split(";")) {
            String[] words = vertex.trim().split(" ");
            if (words.length >= 2) {
                double lat;
                double lon;
                double alt;
                double sd;
                try {
                    lat = Double.parseDouble(words[0]);
                    lon = Double.parseDouble(words[1]);
                    alt = words.length > 2 ? Double.parseDouble(words[2]) : 0;
                    sd = words.length > 3 ? Double.parseDouble(words[3]) : 0;
                } catch (NumberFormatException e) {
                    continue;
                }
                points.add(new MapPoint(lat, lon, alt, sd));
            }
        }
        return points;
    }

    /**
     * Serializes a list of polyline vertices into a string, in the format
     * appropriate for storing as the result of this form question.
     */
    private String formatPoints(List<MapPoint> points) {
        String result = "";
        if (points.size() > 1) {
            for (MapPoint point : points) {
                // TODO(ping): Remove excess precision when we're ready for the output to change.
                result += String.format(Locale.US, "%s %s %s %s;",
                    Double.toString(point.lat), Double.toString(point.lon),
                    Double.toString(point.alt), Float.toString((float) point.sd));
            }
        }
        return result.trim();
    }

    private void buildDialogs() {
        traceSettingsDialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_geotrace_mode))
            .setView(traceSettingsView)
            .setPositiveButton(getString(R.string.start), (dialog, id) -> {
                startGeoTrace();
                dialog.cancel();
                traceSettingsDialog.dismiss();
            })
            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                dialog.cancel();
                traceSettingsDialog.dismiss();
                playCheck = false;

            })
            .setOnCancelListener(dialog -> playCheck = false)
            .create();

        polygonOrPolylineDialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.polygon_or_polyline))
            .setView(polygonOrPolylineView)
            .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
            .setOnCancelListener(dialog -> {
                dialog.cancel();
                traceSettingsDialog.dismiss();
            })
            .create();

        zoomDialogView = getLayoutInflater().inflate(R.layout.geo_zoom_dialog, null);

        zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(v -> {
            map.zoomToPoint(map.getGpsLocation());
            zoomDialog.dismiss();
        });

        zoomPointButton = zoomDialogView.findViewById(R.id.zoom_saved_location);
        zoomPointButton.setOnClickListener(v -> {
            map.zoomToBoundingBox(map.getPointsOfPoly(featureId), 0.6);
            zoomDialog.dismiss();
        });
    }

    private void startGeoTrace() {
        RadioGroup rb = traceSettingsView.findViewById(R.id.radio_group);
        View radioButton = rb.findViewById(rb.getCheckedRadioButtonId());
        int idx = rb.indexOfChild(radioButton);
        beenPaused = true;
        traceMode = idx;
        if (traceMode == 0) {
            setupManualMode();
        } else if (traceMode == 1) {
            setupAutomaticMode();
        } else {
            playCheck = false;
        }
        playButton.setVisibility(View.GONE);
        clearButton.setEnabled(false);
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
        if (units.equals(getString(R.string.minutes))) {
            timeDelay = Long.parseLong(delay) * 60;
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

    public void setGeoTraceScheduler(long delay, TimeUnit units) {
        schedulerHandler = scheduler.scheduleAtFixedRate(
            () -> runOnUiThread(() -> addVertex()), delay, delay, units);
    }

    @SuppressWarnings("unused")  // the "map" parameter is intentionally unused
    private void onGpsLocationReady(MapFragment map) {
        zoomButton.setEnabled(true);
        playButton.setEnabled(true);
        if (getWindow().isActive()) {
            showZoomDialog();
        }
    }

    private void onGpsLocation(MapPoint point) {
        if (modeActive) {
            map.setCenter(point);
        }
    }

    private void addVertex() {
        MapPoint point = map.getGpsLocation();
        if (point != null) {
            map.appendPointToPoly(featureId, point);
        }
    }

    private void clear() {
        map.clearFeatures();
        featureId = map.addDraggablePoly(new ArrayList<>(), false);
        clearButton.setEnabled(false);
        pauseButton.setVisibility(View.GONE);
        manualButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
        playButton.setEnabled(true);
        modeActive = false;
        playCheck = false;
        beenPaused = false;
    }

    private void showClearDialog() {
        if (!map.getPointsOfPoly(featureId).isEmpty()) {
            new AlertDialog.Builder(this)
                .setMessage(R.string.geo_clear_warning)
                .setPositiveButton(R.string.clear, (dialog, id) -> clear())
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
    }

    public void showZoomDialog() {
        if (zoomDialog == null) {
            zoomDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.zoom_to_where))
                .setView(zoomDialogView)
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
                .setOnCancelListener(dialog -> {
                    dialog.cancel();
                    zoomDialog.dismiss();
                })
                .create();
        }

        if (map.getGpsLocation() != null) {
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(themeUtils.getPrimaryTextColor());
        } else {
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }
        if (!map.getPointsOfPoly(featureId).isEmpty()) {
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

    @VisibleForTesting public ImageButton getPlayButton() {
        return playButton;
    }

    @VisibleForTesting public MapFragment getMapFragment() {
        return map;
    }
}
