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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.MappableFormInstance;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.geo.MapFragment;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.instances.DatabaseInstancesRepository;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.MapsPreferences;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/** Show a map with points representing saved instances of the selected form. */
public class FormMapActivity extends BaseGeoMapActivity {
    public static final String MAP_CENTER_KEY = "map_center";
    public static final String MAP_ZOOM_KEY = "map_zoom";

    private FormMapViewModel viewModel;

    @VisibleForTesting public MapFragment map;

    /**
     * Quick lookup of instance objects from map feature IDs.
     */
    private final Map<Integer, FormMapViewModel.MappableFormInstance> instancesByFeatureId = new HashMap<>();

    /**
     * Points to be mapped. Note: kept separately from {@link #instancesByFeatureId} so we can
     * quickly zoom to bounding box.
     */
    private final List<MapPoint> points = new ArrayList<>();

    /**
     * True if the map viewport has been initialized, false otherwise.
     */
    private boolean viewportInitialized;

    @VisibleForTesting public ViewModelProvider.Factory viewModelFactory;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FormsDao dao = new FormsDao();
        Form form = null;
        List<Form> forms = dao.getFormsFromCursor(dao.getFormsCursor(getIntent().getData()));
        if (forms.size() == 1) {
            form = forms.get(0);
        } else {
            // This shouldn't be possible because the instance URI had to be used in the calling
            // activity but it has been logged to Crashlytics.
            finish();
        }

        if (viewModelFactory == null) { // tests set their factories directly
            viewModelFactory = new FormMapActivity.FormMapViewModelFactory(form, new DatabaseInstancesRepository());
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FormMapViewModel.class);

        Timber.i("Starting FormMapActivity for form \"%s\" (jrFormId = \"%s\")",
                viewModel.getFormTitle(),
                viewModel.getFormId());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.instance_map_layout);

        TextView titleView = findViewById(R.id.form_title);
        titleView.setText(viewModel.getFormTitle());

        if (map == null) { // tests set their maps directly
            Context context = getApplicationContext();
            map = MapProvider.createMapFragment(context);
        }

        if (map != null) {
            map.addTo(this, R.id.map_container, this::initMap, this::finish);
        } else {
            finish(); // The configured map provider is not available
        }
    }

    @Override public void onResume() {
        super.onResume();
        updateInstanceGeometry();
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
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    public void initMap(MapFragment newMapFragment) {
        map = newMapFragment;

        findViewById(R.id.zoom_to_location).setOnClickListener(v ->
            map.zoomToPoint(map.getGpsLocation(), true));

        findViewById(R.id.zoom_to_bounds).setOnClickListener(v ->
            map.zoomToBoundingBox(points, 0.8, false));

        findViewById(R.id.layer_menu).setOnClickListener(v -> {
            MapsPreferences.showReferenceLayerDialog(this);
        });

        findViewById(R.id.new_instance).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_EDIT, getIntent().getData()));
        });

        map.setGpsLocationEnabled(true);
        map.setGpsLocationListener(this::onLocationChanged);

        if (previousState != null) {
            restoreFromInstanceState(previousState);
        }

        map.setFeatureClickListener(this::onFeatureClicked);
        updateInstanceGeometry();
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

        List<MappableFormInstance> instances = viewModel.getMappableFormInstances();
        for (MappableFormInstance instance : instances) {
            MapPoint point = new MapPoint(instance.getLatitude(), instance.getLongitude());
            int featureId = map.addMarker(point, false);

            int drawableId = getDrawableIdForStatus(instance.getStatus());
            map.setMarkerIcon(featureId, drawableId);

            instancesByFeatureId.put(featureId, instance);
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
     * Reacts to a tap on a feature by showing a toast or switching activities to view or edit a form.
     */
    public void onFeatureClicked(int featureId) {
        MappableFormInstance instance = instancesByFeatureId.get(featureId);
        ClickAction clickAction = instance == null ? FormMapViewModel.ClickAction.NONE : instance.getClickAction();

        boolean canEditSaved = (Boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_EDIT_SAVED);

        switch (clickAction) {
            case DELETED_TOAST:
                String deletedTime = getString(R.string.deleted_on_date_at_time);
                String disabledMessage = new SimpleDateFormat(deletedTime,
                        Locale.getDefault()).format(viewModel.getDeletedDateOf(instance.getDatabaseId()));

                ToastUtils.showLongToast(disabledMessage);
                break;
            case NOT_VIEWABLE_TOAST:
                ToastUtils.showLongToast(R.string.cannot_edit_completed_form);
                break;
            case OPEN_READ_ONLY:
                startActivity(getViewOnlyFormInstanceIntentFor(featureId));
                break;
            case OPEN_EDIT:
                if (canEditSaved) {
                    startActivity(getEditFormInstanceIntentFor(featureId));
                } else {
                    startActivity(getViewOnlyFormInstanceIntentFor(featureId));
                }
                break;
        }
    }

    private Intent getEditFormInstanceIntentFor(int featureId) {
        Uri uri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, instancesByFeatureId.get(featureId).getDatabaseId());
        return new Intent(Intent.ACTION_EDIT, uri);
    }

    private Intent getViewOnlyFormInstanceIntentFor(int featureId) {
        Intent intent = getEditFormInstanceIntentFor(featureId);
        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
        return intent;
    }

    protected void restoreFromInstanceState(Bundle state) {
        MapPoint mapCenter = state.getParcelable(MAP_CENTER_KEY);
        double mapZoom = state.getDouble(MAP_ZOOM_KEY);
        if (mapCenter != null) {
            map.zoomToPoint(mapCenter, mapZoom, false);
            viewportInitialized = true; // avoid recentering as soon as location is received
        }
    }

    /**
     * Build {@link FormMapViewModel} and its dependencies.
     */
    private class FormMapViewModelFactory implements ViewModelProvider.Factory {
        private final Form form;
        private final InstancesRepository instancesRepository;

        FormMapViewModelFactory(Form form, InstancesRepository instancesRepository) {
            this.form = form;
            this.instancesRepository = instancesRepository;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormMapViewModel(form, instancesRepository);
        }
    }

    private static int getDrawableIdForStatus(String status) {
        switch (status) {
            case InstanceProviderAPI.STATUS_INCOMPLETE:
                return R.drawable.ic_room_blue_24dp;
            case InstanceProviderAPI.STATUS_COMPLETE:
                return R.drawable.ic_room_deep_purple_24dp;
            case InstanceProviderAPI.STATUS_SUBMITTED:
                return R.drawable.ic_room_green_24dp;
            case InstanceProviderAPI.STATUS_SUBMISSION_FAILED:
                return R.drawable.ic_room_red_24dp;
        }
        return R.drawable.ic_map_point;
    }
}
