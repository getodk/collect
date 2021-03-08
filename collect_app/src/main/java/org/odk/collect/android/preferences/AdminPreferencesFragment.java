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

package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import org.odk.collect.android.R;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog;
import org.odk.collect.android.fragments.dialogs.SimpleDialog;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;

import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog.MOVING_BACKWARDS_DIALOG_TAG;
import static org.odk.collect.android.preferences.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_GET_BLANK;
import static org.odk.collect.android.preferences.AdminKeys.KEY_IMPORT_SETTINGS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_JUMP_TO;
import static org.odk.collect.android.preferences.AdminKeys.KEY_MOVING_BACKWARDS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SAVE_MID;
import static org.odk.collect.android.preferences.GeneralKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.odk.collect.android.preferences.utilities.PreferencesUtils.displayDisabled;

public class AdminPreferencesFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String ADMIN_PREFERENCES = "admin_prefs";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

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
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
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
                    displayPreferences(new MainMenuAccessPreferences());
                    break;
                case "user_settings":
                    displayPreferences(new UserSettingsAccessPreferences());
                    break;
                case "form_entry":
                    displayPreferences(new FormEntryAccessPreferences());
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

    public static class MainMenuAccessPreferences extends BasePreferenceFragment {

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            DaggerUtils.getComponent(context).inject(this);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

            setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey);
            findPreference(KEY_EDIT_SAVED).setEnabled(preferencesDataSourceProvider.getAdminPreferences().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));

            FormUpdateMode formUpdateMode = getFormUpdateMode(requireContext(), preferencesDataSourceProvider.getGeneralPreferences());
            if (formUpdateMode == FormUpdateMode.MATCH_EXACTLY) {
                displayDisabled(findPreference(KEY_GET_BLANK), false);
            }
        }
    }

    public static class UserSettingsAccessPreferences extends BasePreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);
            setPreferencesFromResource(R.xml.user_settings_access_preferences, rootKey);
        }
    }

    public static class FormEntryAccessPreferences extends BasePreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

            addPreferencesFromResource(R.xml.form_entry_access_preferences);

            findPreference(KEY_MOVING_BACKWARDS).setOnPreferenceChangeListener((preference, newValue) -> {
                if (((CheckBoxPreference) preference).isChecked()) {
                    new MovingBackwardsDialog().show(getActivity().getSupportFragmentManager(), MOVING_BACKWARDS_DIALOG_TAG);
                } else {
                    SimpleDialog.newInstance(getActivity().getString(R.string.moving_backwards_enabled_title), 0, getActivity().getString(R.string.moving_backwards_enabled_message), getActivity().getString(R.string.ok), false).show(((AdminPreferencesActivity) getActivity()).getSupportFragmentManager(), SimpleDialog.COLLECT_DIALOG_TAG);
                    onMovingBackwardsEnabled();
                }
                return true;
            });
            findPreference(KEY_JUMP_TO).setEnabled(preferencesDataSourceProvider.getAdminPreferences().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
            findPreference(KEY_SAVE_MID).setEnabled(preferencesDataSourceProvider.getAdminPreferences().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
        }

        private void preventOtherWaysOfEditingForm() {
            preferencesDataSourceProvider.getAdminPreferences().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, false);
            preferencesDataSourceProvider.getAdminPreferences().save(KEY_EDIT_SAVED, false);
            preferencesDataSourceProvider.getAdminPreferences().save(KEY_SAVE_MID, false);
            preferencesDataSourceProvider.getAdminPreferences().save(KEY_JUMP_TO, false);
            preferencesDataSourceProvider.getGeneralPreferences().save(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR, CONSTRAINT_BEHAVIOR_ON_SWIPE);

            findPreference(KEY_JUMP_TO).setEnabled(false);
            findPreference(KEY_SAVE_MID).setEnabled(false);

            ((CheckBoxPreference) findPreference(KEY_JUMP_TO)).setChecked(false);
            ((CheckBoxPreference) findPreference(KEY_SAVE_MID)).setChecked(false);
        }

        private void onMovingBackwardsEnabled() {
            preferencesDataSourceProvider.getAdminPreferences().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, true);
            findPreference(KEY_JUMP_TO).setEnabled(true);
            findPreference(KEY_SAVE_MID).setEnabled(true);
        }
    }

    public void preventOtherWaysOfEditingForm() {
        FormEntryAccessPreferences fragment = (FormEntryAccessPreferences) getFragmentManager().findFragmentById(R.id.preferences_fragment_container);
        fragment.preventOtherWaysOfEditingForm();
    }
}