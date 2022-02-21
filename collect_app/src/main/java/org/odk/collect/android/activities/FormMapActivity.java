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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.MappableFormInstance;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.geo.MappableSelectItem;
import org.odk.collect.geo.MappableSelectItem.IconifiedText;
import org.odk.collect.geo.SelectionMapActivity;
import org.odk.collect.geo.SelectionSummarySheet;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapFragmentFactory;
import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProtectedProjectKeys;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

/**
 * Show a map with points representing saved instances of the selected form.
 */
public class FormMapActivity extends SelectionMapActivity {

    public static final String MAP_CENTER_KEY = "map_center";
    public static final String MAP_ZOOM_KEY = "map_zoom";

    public static final String EXTRA_FORM_ID = "form_id";

    protected Bundle previousState;

    private FormMapViewModel viewModel;

    @Inject
    MapFragmentFactory mapFragmentFactory;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    SettingsProvider settingsProvider;

    private MapFragment map;

    public BottomSheetBehavior summarySheetBehavior;
    private SelectionSummarySheet summarySheet;


    private final Map<Integer, MappableSelectItem> items = new HashMap<>();

    /**
     * Points to be mapped. Note: kept separately from {@link #items} so we can
     * quickly zoom to bounding box.
     */
    private final List<MapPoint> points = new ArrayList<>();

    /**
     * True if the map viewport has been initialized, false otherwise.
     */
    private boolean viewportInitialized;

    @VisibleForTesting
    public ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previousState = savedInstanceState;

        DaggerUtils.getComponent(this).inject(this);

        if (!permissionsProvider.areLocationPermissionsGranted()) {
            ToastUtils.showLongToast(this, R.string.not_granted_permission);
            finish();
        }

        Form form = formsRepositoryProvider.get().get(getIntent().getLongExtra(EXTRA_FORM_ID, -1));

        if (viewModelFactory == null) { // tests set their factories directly
            viewModelFactory = new FormMapActivity.FormMapViewModelFactory(form, instancesRepositoryProvider.get());
        }

        viewModel = new ViewModelProvider(this, viewModelFactory).get(FormMapViewModel.class);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.instance_map_layout);
        setUpSummarySheet();

        TextView titleView = findViewById(R.id.form_title);
        titleView.setText(viewModel.getFormTitle());

        MapFragment mapToAdd = mapFragmentFactory.createMapFragment(getApplicationContext());

        if (mapToAdd != null) {
            mapToAdd.addTo(this, R.id.map_container, this::initMap, this::finish);
        } else {
            finish(); // The configured map provider is not available
        }
    }

    private void setUpSummarySheet() {
        summarySheetBehavior = BottomSheetBehavior.from(findViewById(R.id.submission_summary));
        summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        summarySheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                int selectedSubmissionId = viewModel.getSelectedId();
                if (newState == BottomSheetBehavior.STATE_HIDDEN && selectedSubmissionId != -1) {
                    map.setMarkerIcon(selectedSubmissionId, items.get(selectedSubmissionId).getSmallIcon());
                    viewModel.setSelectedId(-1);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        summarySheet = findViewById(R.id.submission_summary);
        summarySheet.setListener((id) -> {
            summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            returnItem(id);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInstanceGeometry();
    }

    @Override
    public void onBackPressed() {
        if (summarySheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    public void initMap(MapFragment newMapFragment) {
        map = newMapFragment;

        findViewById(R.id.zoom_to_location).setOnClickListener(v ->
                map.zoomToPoint(map.getGpsLocation(), true));

        findViewById(R.id.zoom_to_bounds).setOnClickListener(v ->
                map.zoomToBoundingBox(points, 0.8, false));

        findViewById(R.id.layer_menu).setOnClickListener(v -> {
            MapsPreferencesFragment.showReferenceLayerDialog(this);
        });

        findViewById(R.id.new_instance).setOnClickListener(v -> {
            createNewItem();
        });

        map.setGpsLocationEnabled(true);
        map.setGpsLocationListener(this::onLocationChanged);

        if (previousState != null) {
            restoreFromInstanceState(previousState);
        }

        map.setFeatureClickListener(this::onFeatureClicked);
        map.setClickListener(this::onClick);
        updateInstanceGeometry();

        if (viewModel.getSelectedId() != -1) {
            onFeatureClicked(viewModel.getSelectedId());
        }
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void onClick(MapPoint mapPoint) {
        if (summarySheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void updateInstanceGeometry() {
        if (map == null) {
            return;
        }

        updateMapFeatures();

        if (!viewportInitialized && !points.isEmpty()) {
            map.zoomToBoundingBox(points, 0.8, false);
            viewportInitialized = true;
        }

        TextView statusView = findViewById(R.id.geometry_status);
        statusView.setText(getString(R.string.geometry_status, viewModel.getTotalInstanceCount(), points.size()));
    }

    /**
     * Clears the existing features on the map and places features for the current form's instances.
     */
    private void updateMapFeatures() {
        points.clear();
        map.clearFeatures();
        items.clear();

        List<MappableFormInstance> instances = viewModel.getMappableFormInstances();
        for (MappableFormInstance instance : instances) {
            MappableSelectItem item = convertItem(instance);
            MapPoint point = new MapPoint(item.getLatitude(), item.getLongitude());
            int featureId = map.addMarker(point, false, MapFragment.BOTTOM);
            map.setMarkerIcon(featureId, featureId == viewModel.getSelectedId() ? item.getLargeIcon() : item.getSmallIcon());

            items.put(featureId, item);
            points.add(point);
        }
    }

    /**
     * Zooms the map to the new location if the map viewport hasn't been initialized yet.
     */
    public void onLocationChanged(MapPoint point) {
        if (!viewportInitialized) {
            map.zoomToPoint(point, true);
            viewportInitialized = true;
        }
    }

    /**
     * Reacts to a tap on a feature by showing a submission summary.
     */
    public void onFeatureClicked(int featureId) {
        summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (!isSummaryForGivenSubmissionDisplayed(featureId)) {
            removeEnlargedMarkerIfExist(featureId);

            MappableSelectItem item = items.get(featureId);
            if (item != null) {
                map.zoomToPoint(new MapPoint(item.getLatitude(), item.getLongitude()), map.getZoom(), true);
                map.setMarkerIcon(featureId, item.getLargeIcon());
                summarySheet.setItem(item);
                summarySheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            viewModel.setSelectedId(featureId);
        }
    }

    private boolean isSummaryForGivenSubmissionDisplayed(int newSubmissionId) {
        return viewModel.getSelectedId() == newSubmissionId && summarySheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN;
    }

    protected void restoreFromInstanceState(Bundle state) {
        MapPoint mapCenter = state.getParcelable(MAP_CENTER_KEY);
        double mapZoom = state.getDouble(MAP_ZOOM_KEY);
        if (mapCenter != null) {
            map.zoomToPoint(mapCenter, mapZoom, false);
            viewportInitialized = true; // avoid recentering as soon as location is received
        }
    }

    private static int getDrawableIdForStatus(String status, boolean enlarged) {
        switch (status) {
            case Instance.STATUS_INCOMPLETE:
                return enlarged ? R.drawable.ic_room_form_state_incomplete_48dp : R.drawable.ic_room_form_state_incomplete_24dp;
            case Instance.STATUS_COMPLETE:
                return enlarged ? R.drawable.ic_room_form_state_complete_48dp : R.drawable.ic_room_form_state_complete_24dp;
            case Instance.STATUS_SUBMITTED:
                return enlarged ? R.drawable.ic_room_form_state_submitted_48dp : R.drawable.ic_room_form_state_submitted_24dp;
            case Instance.STATUS_SUBMISSION_FAILED:
                return enlarged ? R.drawable.ic_room_form_state_submission_failed_48dp : R.drawable.ic_room_form_state_submission_failed_24dp;
        }
        return R.drawable.ic_map_point;
    }

    @NonNull
    private MappableSelectItem convertItem(MappableFormInstance mappableFormInstance) {
        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(this, mappableFormInstance.getStatus(), mappableFormInstance.getLastStatusChangeDate());

        String info = null;
        switch (mappableFormInstance.getClickAction()) {
            case DELETED_TOAST:
                String deletedTime = getString(R.string.deleted_on_date_at_time);
                info = new SimpleDateFormat(deletedTime,
                        Locale.getDefault()).format(viewModel.getDeletedDateOf(mappableFormInstance.getDatabaseId()));
                break;
            case NOT_VIEWABLE_TOAST:
                info = getString(R.string.cannot_edit_completed_form);
                break;
        }

        IconifiedText action = null;
        switch (mappableFormInstance.getClickAction()) {
            case OPEN_READ_ONLY:
                action = new IconifiedText(R.drawable.ic_visibility, getString(R.string.view_data));
                break;
            case OPEN_EDIT:
                boolean canEditSaved = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED);
                action = new IconifiedText(
                        canEditSaved ? R.drawable.ic_edit : R.drawable.ic_visibility,
                        getString(canEditSaved ? R.string.review_data : R.string.view_data)
                );
                break;
        }

        return new MappableSelectItem(
                mappableFormInstance.getDatabaseId(),
                mappableFormInstance.getLatitude(),
                mappableFormInstance.getLongitude(),
                getDrawableIdForStatus(mappableFormInstance.getStatus(), false),
                getDrawableIdForStatus(mappableFormInstance.getStatus(), true),
                mappableFormInstance.getInstanceName(),
                new IconifiedText(
                        IconUtils.getSubmissionSummaryStatusIcon(mappableFormInstance.getStatus()),
                        instanceLastStatusChangeDate
                ),
                info,
                action
        );
    }

    private void removeEnlargedMarkerIfExist(int newSubmissionId) {
        int selectedSubmissionId = viewModel.getSelectedId();
        if (selectedSubmissionId != -1 && selectedSubmissionId != newSubmissionId) {
            map.setMarkerIcon(selectedSubmissionId, items.get(selectedSubmissionId).getSmallIcon());
        }
    }

    /**
     * Build {@link FormMapViewModel} and its dependencies.
     */
    private class FormMapViewModelFactory implements ViewModelProvider.Factory {
        private final Form form;
        private final InstancesRepository instancesRepository;

        FormMapViewModelFactory(@NonNull Form form, InstancesRepository instancesRepository) {
            this.form = form;
            this.instancesRepository = instancesRepository;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormMapViewModel(form, instancesRepository);
        }
    }
}
