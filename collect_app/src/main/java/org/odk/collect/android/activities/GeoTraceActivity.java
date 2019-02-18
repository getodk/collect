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
import org.odk.collect.android.map.GoogleMapFragment;
import org.odk.collect.android.map.MapFragment;
import org.odk.collect.android.map.MapPoint;
import org.odk.collect.android.map.OsmMapFragment;
import org.odk.collect.android.preferences.GeneralKeys;
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

import static org.odk.collect.android.utilities.PermissionUtils.areLocationPermissionsGranted;

public class GeoTraceActivity extends BaseGeoMapActivity implements IRegisterReceiver {
    public static final String PREF_VALUE_GOOGLE_MAPS = "google_maps";
    public static final String MAP_CENTER_KEY = "map_center";
    public static final String MAP_ZOOM_KEY = "map_zoom";
    public static final String POINTS_KEY = "points";
    public static final String RECORDING_ACTIVE_KEY = "recording_active";
    public static final String RECORDING_MODE_KEY = "recording_mode";
    public static final String TIME_DELAY_KEY = "time_delay";
    public static final String TIME_UNITS_KEY = "time_units";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture schedulerHandler;

    private MapFragment map;
    private int featureId = -1;  // will be a positive featureId once map is ready
    private String originalTraceString = "";

    private ImageButton zoomButton;
    private ImageButton playButton;
    private ImageButton clearButton;
    private Button manualButton;
    private ImageButton pauseButton;
    private ImageButton backspaceButton;

    private View traceSettingsView;
    private View polygonOrPolylineView;

    private AlertDialog traceSettingsDialog;
    private AlertDialog polygonOrPolylineDialog;

    private boolean recordingActive;
    private int recordingMode; // 0 manual, 1 is automatic
    private Spinner timeUnits;
    private Spinner timeDelay;

    private Bundle previousState;

    // restored from savedInstanceState
    private MapPoint restoredMapCenter;
    private Double restoredMapZoom;
    private List<MapPoint> restoredPoints;
    private int restoredTimeDelayIndex = 3;
    private int restoredTimeUnitsIndex;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previousState = savedInstanceState;
        if (savedInstanceState != null) {
            restoredMapCenter = savedInstanceState.getParcelable(MAP_CENTER_KEY);
            restoredMapZoom = savedInstanceState.getDouble(MAP_ZOOM_KEY);
            restoredPoints = savedInstanceState.getParcelableArrayList(POINTS_KEY);
            recordingActive = savedInstanceState.getBoolean(RECORDING_ACTIVE_KEY, false);
            recordingMode = savedInstanceState.getInt(RECORDING_MODE_KEY, 0);
            restoredTimeDelayIndex = savedInstanceState.getInt(TIME_DELAY_KEY, 3);
            restoredTimeUnitsIndex = savedInstanceState.getInt(TIME_UNITS_KEY, 0);
        }

        if (!areLocationPermissionsGranted(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(getString(R.string.geotrace_title));
        setContentView(R.layout.geotrace_layout);
        createMapFragment().addTo(this, R.id.map_container, this::initMap);
    }

    public MapFragment createMapFragment() {
        String mapSdk = getIntent().getStringExtra(GeneralKeys.KEY_MAP_SDK);
        return (mapSdk == null || mapSdk.equals(PREF_VALUE_GOOGLE_MAPS)) ?
            new GoogleMapFragment() : new OsmMapFragment();
    }

    @Override protected void onStart() {
        super.onStart();
        // initMap() is called asynchronously, so map might not be initialized yet.
        if (map != null) {
            map.setGpsLocationEnabled(true);
        }
    }

    @Override protected void onStop() {
        // To avoid a memory leak, we have to shut down GPS when the activity
        // quits for good. But if it's only a screen rotation, we don't want to
        // stop/start GPS and make the user wait to get a GPS lock again.
        if (!isChangingConfigurations()) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            if (map != null) {
                map.setGpsLocationEnabled(false);
            }
        }
        super.onStop();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (map == null) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                state.putAll(previousState);
            }
            return;
        }
        state.putParcelable(MAP_CENTER_KEY, map.getCenter());
        state.putDouble(MAP_ZOOM_KEY, map.getZoom());
        state.putParcelableArrayList(POINTS_KEY, new ArrayList<>(map.getPolyPoints(featureId)));
        state.putBoolean(RECORDING_ACTIVE_KEY, recordingActive);
        state.putInt(RECORDING_MODE_KEY, recordingMode);
        state.putInt(TIME_DELAY_KEY, timeDelay.getSelectedItemPosition());
        state.putInt(TIME_UNITS_KEY, timeUnits.getSelectedItemPosition());
    }

    @Override protected void onDestroy() {
        if (schedulerHandler != null && !schedulerHandler.isCancelled()) {
            schedulerHandler.cancel(true);
        }
        super.onDestroy();
    }

    @Override public void destroy() { }

    public void initMap(MapFragment newMapFragment) {
        if (newMapFragment == null) {  // could not create the map
            finish();
            return;
        }
        if (newMapFragment.getFragment().getActivity() == null) {
            // If the screen is rotated just after the activity starts but
            // before initMap() is called, then when the activity is re-created
            // in the new orientation, initMap() can sometimes be called on the
            // old, dead Fragment that used to be attached to the old activity.
            // Touching the dead Fragment will cause a crash; discard it.
            return;
        }

        map = newMapFragment;
        if (map instanceof GoogleMapFragment) {
            helper = new MapHelper(this, ((GoogleMapFragment) map).getGoogleMap(), selectedLayer);
        } else if (map instanceof OsmMapFragment) {
            helper = new MapHelper(this, ((OsmMapFragment) map).getMapView(), this, selectedLayer);
        }
        helper.setBasemap();

        traceSettingsView = getLayoutInflater().inflate(R.layout.geotrace_dialog, null);
        RadioGroup group = traceSettingsView.findViewById(R.id.radio_group);
        group.check(group.getChildAt(recordingMode).getId());
        timeDelay = traceSettingsView.findViewById(R.id.trace_delay);
        timeDelay.setSelection(restoredTimeDelayIndex);
        timeUnits = traceSettingsView.findViewById(R.id.trace_scale);
        timeUnits.setSelection(restoredTimeUnitsIndex);

        polygonOrPolylineView = getLayoutInflater().inflate(R.layout.polygon_polyline_dialog, null);

        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(v -> showClearDialog());

        pauseButton = findViewById(R.id.pause);
        pauseButton.setOnClickListener(v -> {
            recordingActive = false;
            try {
                schedulerHandler.cancel(true);
            } catch (Exception e) {
                // Do nothing
            }
            updateUi();
        });

        backspaceButton = findViewById(R.id.backspace);
        backspaceButton.setOnClickListener(v -> removeLastPoint());

        ImageButton saveButton = findViewById(R.id.geotrace_save);
        saveButton.setOnClickListener(v -> {
            if (!map.getPolyPoints(featureId).isEmpty()) {
                polygonOrPolylineDialog.show();
            } else {
                finishWithResult();
            }
        });

        playButton = findViewById(R.id.play);
        playButton.setOnClickListener(v -> {
            if (map.getPolyPoints(featureId).isEmpty()) {
                traceSettingsDialog.show();
            } else {
                startGeoTrace();
            }
        });

        manualButton = findViewById(R.id.manual_button);
        manualButton.setOnClickListener(v -> appendPoint());

        Button polygonSave = polygonOrPolylineView.findViewById(R.id.polygon_save);
        polygonSave.setOnClickListener(v -> {
            if (map.getPolyPoints(featureId).size() > 2) {
                // Close the polygon.
                map.appendPointToPoly(featureId, map.getPolyPoints(featureId).get(0));
                polygonOrPolylineDialog.dismiss();
                finishWithResult();
            } else {
                polygonOrPolylineDialog.dismiss();
                ToastUtils.showShortToastInMiddle(getString(R.string.polygon_validator));
            }
        });

        Button polylineSave = polygonOrPolylineView.findViewById(R.id.polyline_save);
        polylineSave.setOnClickListener(v -> {
            if (map.getPolyPoints(featureId).size() > 1) {
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
        zoomButton.setOnClickListener(v -> map.zoomToPoint(map.getGpsLocation(), true));

        List<MapPoint> points = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(GeoTraceWidget.TRACE_LOCATION)) {
            originalTraceString = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
            points = parsePoints(originalTraceString);
        }
        if (restoredPoints != null) {
            points = restoredPoints;
        }
        featureId = map.addDraggablePoly(points, false);

        if (recordingActive) {
            startGeoTrace();
        }

        map.setGpsLocationEnabled(true);
        map.setGpsLocationListener(this::onGpsLocation);
        if (restoredMapCenter != null && restoredMapZoom != null) {
            map.zoomToPoint(restoredMapCenter, restoredMapZoom, false);
        } else if (!points.isEmpty()) {
            map.zoomToBoundingBox(points, 0.6, false);
        } else {
            map.runOnGpsLocationReady(this::onGpsLocationReady);
        }
        updateUi();
    }

    private void finishWithResult() {
        List<MapPoint> points = map.getPolyPoints(featureId);
        setResult(RESULT_OK, new Intent().putExtra(
            FormEntryActivity.GEOTRACE_RESULTS, formatPoints(points)));
        finish();
    }

    @Override public void onBackPressed() {
        if (!formatPoints(map.getPolyPoints(featureId)).equals(originalTraceString)) {
            showBackDialog();
        } else {
            finish();
        }
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
        for (MapPoint point : points) {
            // TODO(ping): Remove excess precision when we're ready for the output to change.
            result += String.format(Locale.US, "%s %s %s %s;",
                Double.toString(point.lat), Double.toString(point.lon),
                Double.toString(point.alt), Float.toString((float) point.sd));
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
            })
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
    }

    private void startGeoTrace() {
        RadioGroup rb = traceSettingsView.findViewById(R.id.radio_group);
        View radioButton = rb.findViewById(rb.getCheckedRadioButtonId());
        recordingMode = rb.indexOfChild(radioButton);
        if (recordingMode == 0) {
            setupManualMode();
        } else if (recordingMode == 1) {
            setupAutomaticMode();
        }
        updateUi();
    }

    private void setupManualMode() {
        recordingActive = true;
    }

    private void setupAutomaticMode() {
        String delay = timeDelay.getSelectedItem().toString();
        String units = timeUnits.getSelectedItem().toString();
        long timeDelay;
        TimeUnit timeUnitsValue;
        if (units.equals(getString(R.string.minutes))) {
            timeDelay = Long.parseLong(delay) * 60;
            timeUnitsValue = TimeUnit.SECONDS;

        } else {
            //in Seconds
            timeDelay = Long.parseLong(delay);
            timeUnitsValue = TimeUnit.SECONDS;
        }

        startScheduler(timeDelay, timeUnitsValue);
        recordingActive = true;
    }

    public void updateRecordingMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.trace_manual:
                if (checked) {
                    recordingMode = 0;
                    timeUnits.setVisibility(View.GONE);
                    timeDelay.setVisibility(View.GONE);
                    timeDelay.invalidate();
                    timeUnits.invalidate();
                }
                break;
            case R.id.trace_automatic:
                if (checked) {
                    recordingMode = 1;
                    timeUnits.setVisibility(View.VISIBLE);
                    timeDelay.setVisibility(View.VISIBLE);
                    timeDelay.invalidate();
                    timeUnits.invalidate();
                }
                break;
        }
    }

    public void startScheduler(long delay, TimeUnit units) {
        schedulerHandler = scheduler.scheduleAtFixedRate(
            () -> runOnUiThread(this::appendPoint), 0, delay, units);
    }

    @SuppressWarnings("unused")  // the "map" parameter is intentionally unused
    private void onGpsLocationReady(MapFragment map) {
        if (getWindow().isActive()) {
            map.zoomToPoint(map.getGpsLocation(), true);
        }
        updateUi();
    }

    private void onGpsLocation(MapPoint point) {
        if (recordingActive) {
            map.setCenter(point, false);
        }
    }

    private void appendPoint() {
        MapPoint point = map.getGpsLocation();
        if (point != null) {
            map.appendPointToPoly(featureId, point);
            updateUi();
        }
    }

    private void removeLastPoint() {
        if (featureId != -1) {
            map.removePolyLastPoint(featureId);
            updateUi();
        }
    }

    private void clear() {
        map.clearFeatures();
        featureId = map.addDraggablePoly(new ArrayList<>(), false);
        recordingActive = false;
        updateUi();
    }

    /** Updates the visibility and enabled state of buttons to reflect internal state. */
    private void updateUi() {
        final int numPoints = map.getPolyPoints(featureId).size();
        final MapPoint location = map.getGpsLocation();

        // Visibility state
        playButton.setVisibility(recordingActive ? View.GONE : View.VISIBLE);
        pauseButton.setVisibility(recordingActive ? View.VISIBLE : View.GONE);
        manualButton.setVisibility(recordingActive ? View.VISIBLE : View.GONE);

        // Enabled state
        zoomButton.setEnabled(location != null);
        playButton.setEnabled(location != null);
        backspaceButton.setEnabled(numPoints > 0);
        clearButton.setEnabled(!recordingActive && numPoints > 0);
    }

    private void showClearDialog() {
        if (!map.getPolyPoints(featureId).isEmpty()) {
            new AlertDialog.Builder(this)
                .setMessage(R.string.geo_clear_warning)
                .setPositiveButton(R.string.clear, (dialog, id) -> clear())
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
    }

    private void showBackDialog() {
        new AlertDialog.Builder(this)
            .setMessage(getString(R.string.geo_exit_warning))
            .setPositiveButton(R.string.discard, (dialog, id) -> finish())
            .setNegativeButton(R.string.cancel, null)
            .show();

    }

    @VisibleForTesting public ImageButton getPlayButton() {
        return playButton;
    }

    @VisibleForTesting public MapFragment getMapFragment() {
        return map;
    }
}
