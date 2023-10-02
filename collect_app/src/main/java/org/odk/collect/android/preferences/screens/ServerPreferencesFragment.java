/*
 * Copyright 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.preferences.screens;

import static android.app.Activity.RESULT_OK;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_PROTOCOL;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.backgroundwork.FormUpdateScheduler;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.ServerPreferencesAdder;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;
import org.odk.collect.androidshared.system.PlayServicesChecker;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.androidshared.utils.Validator;
import org.odk.collect.permissions.PermissionListener;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.settings.keys.ProjectKeys;

import javax.inject.Inject;

public class ServerPreferencesFragment extends BaseProjectPreferencesFragment {

    private static final int REQUEST_ACCOUNT_PICKER = 1000;

    private EditTextPreference passwordPreference;

    @Inject
    GoogleAccountsManager accountsManager;

    @Inject
    FormUpdateScheduler formUpdateScheduler;

    @Inject
    PermissionsProvider permissionsProvider;

    private Preference selectedGoogleAccountPreference;
    private boolean allowClickSelectedGoogleAccountPreference = true;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.server_preferences, rootKey);
        initProtocolPrefs();
    }

    private void initProtocolPrefs() {
        ListPreference protocolPref = (ListPreference) findPreference(KEY_PROTOCOL);
        protocolPref.setSummary(protocolPref.getEntry());
        protocolPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (preference.getKey().equals(KEY_PROTOCOL)) {
                String stringValue = (String) newValue;
                ListPreference lpref = (ListPreference) preference;
                String oldValue = lpref.getValue();
                lpref.setValue(stringValue);

                if (!newValue.equals(oldValue)) {
                    getPreferenceScreen().removeAll();
                    addPreferencesFromResource(R.xml.server_preferences);
                    initProtocolPrefs();
                }
            }
            return true;
        });

        if (ProjectKeys.PROTOCOL_GOOGLE_SHEETS.equals(protocolPref.getValue())) {
            addGooglePreferences();
        } else {
            addServerPreferences();
        }
    }

    public void addServerPreferences() {
        if (!new ServerPreferencesAdder(this).add()) {
            return;
        }
        EditTextPreference serverUrlPreference = findPreference(ProjectKeys.KEY_SERVER_URL);
        EditTextPreference usernamePreference = findPreference(ProjectKeys.KEY_USERNAME);
        passwordPreference = findPreference(ProjectKeys.KEY_PASSWORD);

        serverUrlPreference.setOnPreferenceChangeListener(createChangeListener());
        serverUrlPreference.setSummary(serverUrlPreference.getText());

        usernamePreference.setOnPreferenceChangeListener(createChangeListener());
        usernamePreference.setSummary(usernamePreference.getText());

        usernamePreference.setOnBindEditTextListener(editText -> {
            editText.setFilters(new InputFilter[]{new ControlCharacterFilter()});
        });

        passwordPreference.setOnPreferenceChangeListener(createChangeListener());
        maskPasswordSummary(passwordPreference.getText());

        passwordPreference.setOnBindEditTextListener(editText -> {
            editText.setFilters(new InputFilter[]{new ControlCharacterFilter()});
        });
    }

    public void addGooglePreferences() {
        addPreferencesFromResource(R.xml.google_preferences);
        selectedGoogleAccountPreference = findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);

        EditTextPreference googleSheetsUrlPreference = (EditTextPreference) findPreference(
                ProjectKeys.KEY_GOOGLE_SHEETS_URL);
        googleSheetsUrlPreference.setOnBindEditTextListener(editText -> editText.setFilters(new InputFilter[] {new ControlCharacterFilter(), new WhitespaceFilter() }));
        googleSheetsUrlPreference.setOnPreferenceChangeListener(createChangeListener());

        String currentGoogleSheetsURL = googleSheetsUrlPreference.getText();
        if (currentGoogleSheetsURL != null && currentGoogleSheetsURL.length() > 0) {
            googleSheetsUrlPreference.setSummary(currentGoogleSheetsURL + "\n\n"
                    + getString(org.odk.collect.strings.R.string.google_sheets_url_hint));
        }
        initAccountPreferences();
    }

    public void initAccountPreferences() {
        selectedGoogleAccountPreference.setSummary(accountsManager.getLastSelectedAccountIfValid());
        selectedGoogleAccountPreference.setOnPreferenceClickListener(preference -> {
            if (allowClickSelectedGoogleAccountPreference) {
                if (new PlayServicesChecker().isGooglePlayServicesAvailable(getActivity())) {
                    allowClickSelectedGoogleAccountPreference = false;
                    requestAccountsPermission();
                } else {
                    new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(getActivity());
                }
            }
            return true;
        });
    }

    private void requestAccountsPermission() {
        permissionsProvider.requestGetAccountsPermission(getActivity(), new PermissionListener() {
            @Override
            public void granted() {
                Intent intent = accountsManager.getAccountChooserIntent();
                startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
            }

            @Override
            public void denied() {
                allowClickSelectedGoogleAccountPreference = true;
            }
        });
    }

    private Preference.OnPreferenceChangeListener createChangeListener() {
        return (preference, newValue) -> {
            switch (preference.getKey()) {
                case ProjectKeys.KEY_SERVER_URL:
                    String url = newValue.toString();

                    if (Validator.isUrlValid(url)) {
                        preference.setSummary(newValue.toString());
                    } else {
                        ToastUtils.showShortToast(requireContext(), org.odk.collect.strings.R.string.url_error);
                        return false;
                    }
                    break;

                case ProjectKeys.KEY_USERNAME:
                    String username = newValue.toString();

                    // do not allow leading and trailing whitespace
                    if (!username.equals(username.trim())) {
                        ToastUtils.showShortToast(requireContext(), org.odk.collect.strings.R.string.username_error_whitespace);
                        return false;
                    }

                    preference.setSummary(username);
                    return true;

                case ProjectKeys.KEY_PASSWORD:
                    String pw = newValue.toString();

                    // do not allow leading and trailing whitespace
                    if (!pw.equals(pw.trim())) {
                        ToastUtils.showShortToast(requireContext(), org.odk.collect.strings.R.string.password_error_whitespace);
                        return false;
                    }

                    maskPasswordSummary(pw);
                    break;

                case ProjectKeys.KEY_GOOGLE_SHEETS_URL:
                    url = newValue.toString();

                    if (Validator.isUrlValid(url)) {
                        preference.setSummary(url + "\n\n" + getString(org.odk.collect.strings.R.string.google_sheets_url_hint));
                    } else if (url.length() == 0) {
                        preference.setSummary(getString(org.odk.collect.strings.R.string.google_sheets_url_hint));
                    } else {
                        ToastUtils.showShortToast(requireContext(), org.odk.collect.strings.R.string.url_error);
                        return false;
                    }
                    break;
            }
            return true;
        };
    }

    private void maskPasswordSummary(String password) {
        passwordPreference.setSummary(password != null && password.length() > 0
                ? "********"
                : "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountsManager.selectAccount(accountName);
                    selectedGoogleAccountPreference.setSummary(accountName);
                }
                allowClickSelectedGoogleAccountPreference = true;
                break;
        }
    }
}
