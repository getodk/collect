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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog;
import org.odk.collect.android.fragments.dialogs.SimpleDialog;
import org.odk.collect.android.utilities.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog.MOVING_BACKWARDS_DIALOG_TAG;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_IMPORT_SETTINGS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_JUMP_TO;
import static org.odk.collect.android.preferences.AdminKeys.KEY_MOVING_BACKWARDS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SAVE_MID;
import static org.odk.collect.android.preferences.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.PreferenceKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE;

public class AdminPreferencesFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String ADMIN_PREFERENCES = "admin_prefs";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

        addPreferencesFromResource(R.xml.admin_preferences);

        findPreference(KEY_CHANGE_ADMIN_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(KEY_IMPORT_SETTINGS).setOnPreferenceClickListener(this);
        findPreference("main_menu").setOnPreferenceClickListener(this);
        findPreference("user_settings").setOnPreferenceClickListener(this);
        findPreference("form_entry").setOnPreferenceClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.admin_preferences);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        Fragment fragment = null;

        switch (preference.getKey()) {

            case KEY_CHANGE_ADMIN_PASSWORD:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View dialogView = factory.inflate(R.layout.password_dialog_layout, null);
                final EditText passwordEditText = dialogView.findViewById(R.id.pwd_field);
                final CheckBox passwordCheckBox = dialogView.findViewById(R.id.checkBox2);
                passwordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (!passwordCheckBox.isChecked()) {
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        } else {
                            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    }
                });
                builder.setTitle(R.string.change_admin_password);
                builder.setView(dialogView);
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pw = passwordEditText.getText().toString();
                        if (!pw.equals("")) {
                            SharedPreferences.Editor editor = getActivity()
                                    .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, pw);
                            ToastUtils.showShortToast(R.string.admin_password_changed);
                            editor.apply();
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "CHANGED");
                        } else {
                            SharedPreferences.Editor editor = getActivity()
                                    .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, "");
                            editor.apply();
                            ToastUtils.showShortToast(R.string.admin_password_disabled);
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "DISABLED");
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Collect.getInstance().getActivityLogger().logAction(this, "AdminPasswordDialog", "CANCELLED");
                    }
                });

                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();

                break;

            case KEY_IMPORT_SETTINGS:
                fragment = new ShowQRCodeFragment();
                break;

            case "main_menu":
                fragment = new MainMenuAccessPreferences();
                break;
            case "user_settings":
                fragment = new UserSettingsAccessPreferences();
                break;
            case "form_entry":
                fragment = new FormEntryAccessPreferences();
                break;
        }

        if (fragment != null) {
            getActivity().getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        return true;
    }

    public static class MainMenuAccessPreferences extends BasePreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

            addPreferencesFromResource(R.xml.main_menu_access_preferences);
            findPreference(KEY_EDIT_SAVED).setEnabled((Boolean) AdminSharedPreferences.getInstance().get(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            toolbar.setTitle(R.string.main_menu_settings);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (toolbar != null) {
                toolbar.setTitle(R.string.admin_preferences);
            }
        }
    }

    public static class UserSettingsAccessPreferences extends BasePreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

            addPreferencesFromResource(R.xml.user_settings_access_preferences);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            toolbar.setTitle(R.string.user_settings);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (toolbar != null) {
                toolbar.setTitle(R.string.admin_preferences);
            }
        }
    }

    public static class FormEntryAccessPreferences extends BasePreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(ADMIN_PREFERENCES);

            addPreferencesFromResource(R.xml.form_entry_access_preferences);

            findPreference(KEY_MOVING_BACKWARDS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (((CheckBoxPreference) preference).isChecked()) {
                        new MovingBackwardsDialog().show(((AdminPreferencesActivity) getActivity()).getSupportFragmentManager(), MOVING_BACKWARDS_DIALOG_TAG);
                    } else {
                        SimpleDialog.newInstance(getActivity().getString(R.string.moving_backwards_enabled_title), 0, getActivity().getString(R.string.moving_backwards_enabled_message), getActivity().getString(R.string.ok), false).show(((AdminPreferencesActivity) getActivity()).getSupportFragmentManager(), SimpleDialog.COLLECT_DIALOG_TAG);
                        onMovingBackwardsEnabled();
                    }
                    return true;
                }
            });
            findPreference(KEY_JUMP_TO).setEnabled((Boolean) AdminSharedPreferences.getInstance().get(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
            findPreference(KEY_SAVE_MID).setEnabled((Boolean) AdminSharedPreferences.getInstance().get(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            toolbar.setTitle(R.string.form_entry_setting);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (toolbar != null) {
                toolbar.setTitle(R.string.admin_preferences);
            }
        }

        private void preventOtherWaysOfEditingForm() {
            AdminSharedPreferences.getInstance().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, false);
            AdminSharedPreferences.getInstance().save(KEY_EDIT_SAVED, false);
            AdminSharedPreferences.getInstance().save(KEY_SAVE_MID, false);
            AdminSharedPreferences.getInstance().save(KEY_JUMP_TO, false);
            GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR, CONSTRAINT_BEHAVIOR_ON_SWIPE);

            findPreference(KEY_JUMP_TO).setEnabled(false);
            findPreference(KEY_SAVE_MID).setEnabled(false);

            ((CheckBoxPreference) findPreference(KEY_JUMP_TO)).setChecked(false);
            ((CheckBoxPreference) findPreference(KEY_SAVE_MID)).setChecked(false);
        }

        private void onMovingBackwardsEnabled() {
            AdminSharedPreferences.getInstance().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, true);
            findPreference(KEY_JUMP_TO).setEnabled(true);
            findPreference(KEY_SAVE_MID).setEnabled(true);
        }
    }

    public void preventOtherWaysOfEditingForm() {
        FormEntryAccessPreferences fragment = (FormEntryAccessPreferences) getFragmentManager().findFragmentById(android.R.id.content);
        fragment.preventOtherWaysOfEditingForm();
    }
}
