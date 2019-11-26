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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
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
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import timber.log.Timber;

/** Show a map with points representing saved instances of the selected form. */
public class FormMapActivity extends BaseGeoMapActivity {
    public static final String MAP_CENTER_KEY = "map_center";
    public static final String MAP_ZOOM_KEY = "map_zoom";

    private FormMapViewModel viewModel;

    private MapFragment map;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FormsDao dao = new FormsDao();
        Form form = dao.getFormsFromCursor(dao.getFormsCursor(getIntent().getData())).get(0);
        viewModel = ViewModelProviders.of(this,
                new FormMapActivity.FormMapViewModelFactory(form, new DatabaseInstancesRepository())).get(FormMapViewModel.class);
        Timber.i("Starting FormMapActivity for form \"%s\" (jrFormId = \"%s\")",
                viewModel.getFormTitle(),
                viewModel.getFormId());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.instance_map_layout);

        TextView titleView = findViewById(R.id.form_title);
        titleView.setText(viewModel.getFormTitle());

        Context context = getApplicationContext();
        MapProvider.createMapFragment(context)
            .addTo(this, R.id.map_container, this::initMap, this::finish);
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
            viewModel.mapZoomToBoundingBoxRequested(map));

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

    protected void updateInstanceGeometry() {
        if (map == null) {
            return;
        }

        viewModel.mapUpdateRequested(map);

        TextView statusView = findViewById(R.id.geometry_status);
        statusView.setText(getString(R.string.geometry_status, viewModel.getInstanceCount(), viewModel.getMappedPointCount()));
    }

    public void onLocationChanged(MapPoint point) {
        viewModel.locationChanged(point, map);
    }

    public void onFeatureClicked(int featureId) {
        FormMapViewModel.FeatureStatus status = viewModel.getStatusOfClickedFeature(featureId);

        boolean canEditSaved = (Boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_EDIT_SAVED);

        switch (status) {
            case DELETED:
                String deletedTime = getString(R.string.deleted_on_date_at_time);
                String disabledMessage = new SimpleDateFormat(deletedTime,
                        Locale.getDefault()).format(viewModel.getDeletedDateOf(featureId));

                ToastUtils.showLongToast(disabledMessage);
                break;
            case NOT_VIEWABLE:
                ToastUtils.showLongToast(R.string.cannot_edit_completed_form);
                break;
            case VIEW_ONLY:
                startActivity(getViewOnlyFormInstanceIntentFor(featureId));
                break;
            case EDITABLE:
                if (canEditSaved) {
                    startActivity(getEditFormInstanceIntentFor(featureId));
                } else {
                    startActivity(getViewOnlyFormInstanceIntentFor(featureId));
                }
                break;
        }
    }

    private Intent getEditFormInstanceIntentFor(int featureId) {
        Uri uri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, viewModel.getDatabaseIdOf(featureId));
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
}
