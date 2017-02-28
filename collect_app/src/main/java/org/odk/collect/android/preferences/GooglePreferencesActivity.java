/*
 * Copyright (C) 2014 Nafundi
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.InputFilter;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UrlUtils;

import java.util.ArrayList;

/**
 * Handles Google specific preferences.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class GooglePreferencesActivity extends PreferenceActivity {

    private ListPreference mSelectedGoogleAccountPreference;
    protected EditTextPreference mGoogleSheetsUrlPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.google_preferences);

        boolean adminMode = getIntent().getBooleanExtra(PreferencesActivity.INTENT_KEY_ADMIN_MODE,
                false);

        SharedPreferences adminPreferences = getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        mSelectedGoogleAccountPreference = (ListPreference) findPreference(
                PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
        PreferenceCategory googlePreferences = (PreferenceCategory) findPreference(
                getString(R.string.google_preferences));

        // get list of google accounts
        final Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(
                "com.google");
        ArrayList<String> accountEntries = new ArrayList<String>();
        ArrayList<String> accountValues = new ArrayList<String>();

        for (int i = 0; i < accounts.length; i++) {
            accountEntries.add(accounts[i].name);
            accountValues.add(accounts[i].name);
        }
        accountEntries.add(getString(R.string.no_account));
        accountValues.add("");

        mSelectedGoogleAccountPreference.setEntries(accountEntries
                .toArray(new String[accountEntries.size()]));
        mSelectedGoogleAccountPreference.setEntryValues(accountValues
                .toArray(new String[accountValues.size()]));
        mSelectedGoogleAccountPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int index = ((ListPreference) preference).findIndexOfValue(newValue
                                .toString());
                        String value =
                                (String) ((ListPreference) preference).getEntryValues()[index];
                        ((ListPreference) preference).setSummary(value);
                        return true;
                    }
                });
        mSelectedGoogleAccountPreference.setSummary(mSelectedGoogleAccountPreference.getValue());

        boolean googleAccountAvailable = adminPreferences.getBoolean(
                AdminKeys.KEY_CHANGE_GOOGLE_ACCOUNT, true);
        if (!(googleAccountAvailable || adminMode)) {
            googlePreferences.removePreference(mSelectedGoogleAccountPreference);
        }

        mGoogleSheetsUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        mGoogleSheetsUrlPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String url = newValue.toString();

                // remove all trailing "/"s
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                if (UrlUtils.isValidUrl(url) || url.length() == 0) {
                    preference.setSummary(newValue.toString());
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.url_error, Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }
            }
        });
        mGoogleSheetsUrlPreference.setSummary(mGoogleSheetsUrlPreference.getText());
        mGoogleSheetsUrlPreference.getEditText().setFilters(new InputFilter[]{
                new ControlCharacterFilter(), new WhitespaceFilter()
        });

    }

}
