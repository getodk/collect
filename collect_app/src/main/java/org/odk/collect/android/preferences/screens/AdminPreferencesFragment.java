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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import org.odk.collect.android.R;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog;
import org.odk.collect.android.preferences.dialogs.ResetDialogPreference;
import org.odk.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_IMPORT_SETTINGS;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class AdminPreferencesFragment extends BaseAdminPreferencesFragment implements Preference.OnPreferenceClickListener {

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        settingsProvider.getAdminSettings().save("project_name", currentProjectProvider.getCurrentProject().getName());
        settingsProvider.getAdminSettings().save("project_icon", currentProjectProvider.getCurrentProject().getIcon());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.admin_preferences, rootKey);

        findPreference("odk_preferences").setOnPreferenceClickListener(this);
        findPreference(KEY_CHANGE_ADMIN_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(KEY_IMPORT_SETTINGS).setOnPreferenceClickListener(this);
        findPreference("main_menu").setOnPreferenceClickListener(this);
        findPreference("user_settings").setOnPreferenceClickListener(this);
        findPreference("form_entry").setOnPreferenceClickListener(this);
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
                    DialogUtils.showIfNotShowing(ChangeAdminPasswordDialog.class,
                            getActivity().getSupportFragmentManager());
                    break;

                case KEY_IMPORT_SETTINGS:
                    Intent pref = new Intent(getActivity(), QRCodeTabsActivity.class);
                    startActivity(pref);
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
}