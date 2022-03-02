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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.MappableFormInstance;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.formmanagement.FormNavigator;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.geo.MappableSelectItem;
import org.odk.collect.geo.MappableSelectItem.IconifiedText;
import org.odk.collect.geo.SelectionMapFragment;
import org.odk.collect.geo.SelectionMapViewModel;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProtectedProjectKeys;
import org.odk.collect.strings.localization.LocalizedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Show a map with points representing saved instances of the selected form.
 */
public class FormMapActivity extends LocalizedActivity {

    public static final String EXTRA_FORM_ID = "form_id";

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    private FormMapViewModel formMapViewModel;

    private SelectionMapViewModel selectionMapViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.form_map_activity);

        Form form = loadForm();

        selectionMapViewModel = new ViewModelProvider(this).get(SelectionMapViewModel.class);
        selectionMapViewModel.setMapTitle(form.getDisplayName());

        FormNavigator formNavigator = new FormNavigator(currentProjectProvider, settingsProvider, instancesRepositoryProvider::get);

        getSupportFragmentManager().setFragmentResultListener(SelectionMapFragment.REQUEST_SELECT_ITEM, this, (requestKey, result) -> {
            if (result.containsKey(SelectionMapFragment.RESULT_SELECTED_ITEM)) {
                long instanceId = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM);
                formNavigator.editInstance(this, instanceId);
            } else if (result.containsKey(SelectionMapFragment.RESULT_CREATE_NEW_ITEM)) {
                formNavigator.newInstance(this, form.getDbId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<MappableSelectItem> items = getItems();
        selectionMapViewModel.setItems(getTotalInstanceCount(), items);
    }

    @Nullable
    private Form loadForm() {
        Form form = formsRepositoryProvider.get().get(getIntent().getLongExtra(EXTRA_FORM_ID, -1));
        FormMapViewModelFactory viewModelFactory = new FormMapViewModelFactory(form, instancesRepositoryProvider.get());
        formMapViewModel = new ViewModelProvider(this, viewModelFactory).get(FormMapViewModel.class);
        return form;
    }

    @NonNull
    private List<MappableSelectItem> getItems() {
        List<MappableFormInstance> instances = formMapViewModel.getMappableFormInstances();
        List<MappableSelectItem> items = new ArrayList<>();
        for (MappableFormInstance instance : instances) {
            items.add(convertItem(instance));
        }

        return items;
    }

    private int getTotalInstanceCount() {
        return formMapViewModel.getTotalInstanceCount();
    }

    @NonNull
    private MappableSelectItem convertItem(MappableFormInstance mappableFormInstance) {
        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(this, mappableFormInstance.getStatus(), mappableFormInstance.getLastStatusChangeDate());

        String info = null;
        switch (mappableFormInstance.getClickAction()) {
            case DELETED_TOAST:
                String deletedTime = getString(R.string.deleted_on_date_at_time);
                info = new SimpleDateFormat(deletedTime,
                        Locale.getDefault()).format(formMapViewModel.getDeletedDateOf(mappableFormInstance.getDatabaseId()));
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
                        getSubmissionSummaryStatusIcon(mappableFormInstance.getStatus()),
                        instanceLastStatusChangeDate
                ),
                info,
                action
        );
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

    public static int getSubmissionSummaryStatusIcon(String instanceStatus) {
        switch (instanceStatus) {
            case Instance.STATUS_INCOMPLETE:
                return R.drawable.form_state_saved;
            case Instance.STATUS_COMPLETE:
                return R.drawable.form_state_finalized;
            case Instance.STATUS_SUBMITTED:
                return R.drawable.form_state_submited;
            case Instance.STATUS_SUBMISSION_FAILED:
                return R.drawable.form_state_submission_failed;
        }

        throw new IllegalArgumentException();
    }

    public void onFeatureClicked(int featureId) {
        Fragment fragment = getSupportFragmentManager().getFragments().stream().filter(fragment1 -> {
            return fragment1.getClass() == SelectionMapFragment.class;
        }).findFirst().get();

        ((SelectionMapFragment) fragment).onFeatureClicked(featureId);
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
