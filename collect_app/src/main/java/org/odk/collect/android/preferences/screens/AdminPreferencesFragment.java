/*
 * Copyright (C) 2017 Shobhit
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

package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.ActivityUtils;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.projects.DeleteProjectResult;
import org.odk.collect.androidshared.ColorPickerViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog;
import org.odk.collect.android.preferences.dialogs.ResetDialogPreference;
import org.odk.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectDeleter;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.androidshared.OneSignTextWatcher;
import org.odk.collect.projects.Project;
import org.odk.collect.androidshared.ColorPickerDialog;
import org.odk.collect.projects.ProjectsRepository;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_IMPORT_SETTINGS;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class AdminPreferencesFragment extends BaseAdminPreferencesFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final String PROJECT_NAME_KEY = "project_name";
    public static final String PROJECT_ICON_KEY = "project_icon";
    public static final String PROJECT_COLOR_KEY = "project_color";
    public static final String DELETE_PROJECT_KEY = "delete_project";

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    ProjectDeleter projectDeleter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        ColorPickerViewModel colorPickerViewModel = new ViewModelProvider(requireActivity()).get(ColorPickerViewModel.class);
        colorPickerViewModel.getPickedColor().observe(this, color -> {
            Project.Saved currentProject = currentProjectProvider.getCurrentProject();
            projectsRepository.save(new Project.Saved(currentProject.getUuid(), currentProject.getName(), currentProject.getIcon(), color));
            findPreference(PROJECT_COLOR_KEY).setSummaryProvider(new ProjectDetailsSummaryProvider(PROJECT_COLOR_KEY, currentProjectProvider));
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.admin_preferences, rootKey);

        findPreference("odk_preferences").setOnPreferenceClickListener(this);
        findPreference(KEY_CHANGE_ADMIN_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(PROJECT_COLOR_KEY).setOnPreferenceClickListener(this);
        findPreference(KEY_IMPORT_SETTINGS).setOnPreferenceClickListener(this);
        findPreference("main_menu").setOnPreferenceClickListener(this);
        findPreference("user_settings").setOnPreferenceClickListener(this);
        findPreference("form_entry").setOnPreferenceClickListener(this);
        findPreference(DELETE_PROJECT_KEY).setOnPreferenceClickListener(this);

        findPreference(PROJECT_NAME_KEY).setSummaryProvider(new ProjectDetailsSummaryProvider(PROJECT_NAME_KEY, currentProjectProvider));
        findPreference(PROJECT_ICON_KEY).setSummaryProvider(new ProjectDetailsSummaryProvider(PROJECT_ICON_KEY, currentProjectProvider));
        findPreference(PROJECT_COLOR_KEY).setSummaryProvider(new ProjectDetailsSummaryProvider(PROJECT_COLOR_KEY, currentProjectProvider));

        findPreference(PROJECT_NAME_KEY).setOnPreferenceChangeListener(this);
        findPreference(PROJECT_ICON_KEY).setOnPreferenceChangeListener(this);

        ((EditTextPreference) findPreference(PROJECT_NAME_KEY)).setText(currentProjectProvider.getCurrentProject().getName());
        ((EditTextPreference) findPreference(PROJECT_ICON_KEY)).setText(currentProjectProvider.getCurrentProject().getIcon());

        ((EditTextPreference) findPreference(PROJECT_ICON_KEY)).setOnBindEditTextListener(editText -> editText.addTextChangedListener(new OneSignTextWatcher(editText)));
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            ResetDialogPreference resetDialogPreference = null;
            if (preference instanceof ResetDialogPreference) {
                resetDialogPreference = (ResetDialogPreference) preference;
            }
            if (resetDialogPreference != null) {
                ResetDialogPreferenceFragmentCompat dialogFragment = ResetDialogPreferenceFragmentCompat.newInstance(preference.getKey());
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getParentFragmentManager(), null);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            switch (preference.getKey()) {
                case "odk_preferences":
                    Intent intent = new Intent(getActivity(), GeneralPreferencesActivity.class);
                    intent.putExtra(INTENT_KEY_ADMIN_MODE, true);
                    startActivity(intent);
                    break;
                case KEY_CHANGE_ADMIN_PASSWORD:
                    DialogUtils.showIfNotShowing(ChangeAdminPasswordDialog.class, getActivity().getSupportFragmentManager());
                    break;
                case PROJECT_COLOR_KEY:
                    Project.Saved project = currentProjectProvider.getCurrentProject();
                    Bundle bundle = new Bundle();
                    bundle.putString(ColorPickerDialog.CURRENT_COLOR, project.getColor());
                    bundle.putString(ColorPickerDialog.CURRENT_ICON, project.getIcon());
                    DialogUtils.showIfNotShowing(ColorPickerDialog.class, bundle, getActivity().getSupportFragmentManager());
                    break;
                case KEY_IMPORT_SETTINGS:
                    Intent pref = new Intent(getActivity(), QRCodeTabsActivity.class);
                    startActivity(pref);
                    break;
                case DELETE_PROJECT_KEY:
                    new AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.delete_project_confirm_message)
                            .setNegativeButton(R.string.delete_project_no, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.delete_project_yes, (dialog, which) -> deleteProject())
                            .show();
                    break;
                case "main_menu":
                    displayPreferences(new MainMenuAccessPreferencesFragment());
                    break;
                case "user_settings":
                    displayPreferences(new UserSettingsAccessPreferencesFragment());
                    break;
                case "form_entry":
                    displayPreferences(new FormEntryAccessPreferencesFragment());
                    break;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Project.Saved currentProject = currentProjectProvider.getCurrentProject();
        switch (preference.getKey()) {
            case PROJECT_NAME_KEY:
                projectsRepository.save(new Project.Saved(currentProject.getUuid(), String.valueOf(newValue), currentProject.getIcon(), currentProject.getColor()));
                break;
            case PROJECT_ICON_KEY:
                projectsRepository.save(new Project.Saved(currentProject.getUuid(), currentProject.getName(), String.valueOf(newValue), currentProject.getColor()));
                break;
        }
        return true;
    }

    private void displayPreferences(Fragment fragment) {
        if (fragment != null) {
            fragment.setArguments(getArguments());
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.preferences_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void preventOtherWaysOfEditingForm() {
        FormEntryAccessPreferencesFragment fragment = (FormEntryAccessPreferencesFragment) getFragmentManager().findFragmentById(R.id.preferences_fragment_container);
        fragment.preventOtherWaysOfEditingForm();
    }

    private static class ProjectDetailsSummaryProvider implements Preference.SummaryProvider<Preference> {
        private final String key;
        private final CurrentProjectProvider currentProjectProvider;

        ProjectDetailsSummaryProvider(String key, CurrentProjectProvider currentProjectProvider) {
            this.key = key;
            this.currentProjectProvider = currentProjectProvider;
        }

        @Override
        public CharSequence provideSummary(Preference preference) {
            switch (key) {
                case PROJECT_NAME_KEY:
                    return currentProjectProvider.getCurrentProject().getName();
                case PROJECT_ICON_KEY:
                    return currentProjectProvider.getCurrentProject().getIcon();
                case PROJECT_COLOR_KEY:
                    Spannable summary = new SpannableString("■");
                    summary.setSpan(new ForegroundColorSpan(Color.parseColor(currentProjectProvider.getCurrentProject().getColor())), 0, summary.length(), 0);
                    return summary;
                default:
                    return null;
            }
        }
    }

    public void deleteProject() {
        DeleteProjectResult deleteProjectResult = projectDeleter.deleteCurrentProject();

        if (deleteProjectResult instanceof DeleteProjectResult.UnsentInstances) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.cannot_delete_project_title)
                    .setMessage(R.string.cannot_delete_project_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        } else if (deleteProjectResult instanceof DeleteProjectResult.DeletedSuccessfully) {
            Project.Saved newCurrentProject = ((DeleteProjectResult.DeletedSuccessfully) deleteProjectResult).getProject();
            if (newCurrentProject != null) {
                ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);
                ToastUtils.showLongToast(getString(R.string.switched_project, newCurrentProject.getName()));
            } else {
                ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), SplashScreenActivity.class);
            }
        }
    }
}
