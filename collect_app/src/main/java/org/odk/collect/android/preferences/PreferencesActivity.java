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

package org.odk.collect.android.preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.text.InputFilter;
import android.text.Spanned;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.javarosa.core.services.IPropertyManager;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.MediaUtils;
import static org.odk.collect.android.preferences.PreferenceKeys.*;
import static org.odk.collect.android.preferences.AdminAndGeneralKeys.ag;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles general preferences.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com;
 *         constraint behavior option)
 */
public class PreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
    protected static final int IMAGE_CHOOSER = 0;

    /** Encapsulate the findPreference deprecation warning */
    Preference pref(String key) {
        return findPreference(key);
    }

    /** Allow shorter code to get ListPreferences */
    private ListPreference listPref(String key) {
        return (ListPreference) pref(key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        setTitle(getString(R.string.general_preferences));

        final boolean adminMode = getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        removeAllDisabledPrefs();

        // ToDo: order these logically
        initProtocolPrefs(adminMode);
        initGoogleAccountPref();
        initUserAndPasswordPrefs();
        initNavigationPrefs();
        initConstraintBehaviorPref();
        initFontSizePref();
        initAnalyticsPref();
        initSplashPrefs();
        initMapPrefs();
    }

    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover(this);
        preferencesRemover.remove(
                ag(AdminKeys.KEY_AUTOSEND_WIFI),
                ag(AdminKeys.KEY_AUTOSEND_NETWORK),
                ag(AdminKeys.KEY_CHANGE_SERVER),
                ag(AdminKeys.KEY_CHANGE_PROTOCOL_SETTINGS),
                ag(AdminKeys.KEY_DEFAULT_TO_FINALIZED),
                ag(AdminKeys.KEY_DELETE_AFTER_SEND),
                ag(AdminKeys.KEY_HIGH_RESOLUTION),
                ag(AdminKeys.KEY_SHOW_SPLASH_SCREEN, KEY_SHOW_SPLASH),
                ag(AdminKeys.KEY_SHOW_SPLASH_SCREEN, KEY_SPLASH_PATH),
                ag(AdminKeys.KEY_ANALYTICS),
                ag(AdminKeys.KEY_CHANGE_FONT_SIZE),
                ag(AdminKeys.KEY_CONSTRAINT_BEHAVIOR),
                ag(AdminKeys.KEY_SHOW_MAP_SDK),
                ag(AdminKeys.KEY_SHOW_MAP_BASEMAP),
                ag(AdminKeys.KEY_NAVIGATION),
                ag(AdminKeys.KEY_CHANGE_PASSWORD),
                ag(AdminKeys.KEY_CHANGE_USERNAME),
                ag(AdminKeys.KEY_CHANGE_GOOGLE_ACCOUNT));
        preferencesRemover.removeEmptyCategories();
    }

    private void initAnalyticsPref() {
        final CheckBoxPreference analyticsPreference = (CheckBoxPreference) pref(KEY_ANALYTICS);

        if (analyticsPreference != null) {
            analyticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
                    googleAnalytics.setAppOptOut(!analyticsPreference.isChecked());
                    return true;
                }
            });
        }
    }

    private void initSplashPrefs() {
        final PreferenceScreen pref = (PreferenceScreen) pref(KEY_SPLASH_PATH);

        if (pref != null) {
            pref.setOnPreferenceClickListener(new SplashClickListener(this, pref));
            pref.setSummary(pref.getSharedPreferences().getString(
                    KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
        }
    }

    private void initFontSizePref() {
        final ListPreference pref = listPref(KEY_FONT_SIZE);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                    CharSequence entry = ((ListPreference) preference).getEntries()[index];
                    preference.setSummary(entry);
                    return true;
                }
            });
        }
    }

    private void initConstraintBehaviorPref() {
        final ListPreference pref = listPref(KEY_CONSTRAINT_BEHAVIOR);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(
                    new OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            int index = ((ListPreference) preference).findIndexOfValue(
                                    newValue.toString());
                            CharSequence entry = ((ListPreference) preference).getEntries()[index];
                            preference.setSummary(entry);
                            return true;
                        }
                    });
        }
    }

    private void initMapPrefs() {
        final ListPreference mapSdk = listPref(KEY_MAP_SDK);
        final ListPreference mapBasemap = listPref(KEY_MAP_BASEMAP);

        if (mapSdk == null || mapBasemap == null)
            return;

        mapSdk.setSummary(mapSdk.getEntry());
        mapSdk.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                if (index == ARRAY_INDEX_GOOGLE_MAPS) {
                    mapBasemap.setEntryValues(R.array.map_google_basemap_selector_entry_values);
                    mapBasemap.setEntries(R.array.map_google_basemap_selector_entries);
                    mapBasemap.setValue(GOOGLE_MAPS_BASEMAP_DEFAULT);
                    mapBasemap.setSummary(mapBasemap.getEntry());
                } else {
                    // Else its OSM Maps
                    mapBasemap.setEntryValues(R.array.map_osm_basemap_selector_entry_values);
                    mapBasemap.setEntries(R.array.map_osm_basemap_selector_entries);
                    mapBasemap.setValue(OSM_MAPS_BASEMAP_DEFAULT);
                    mapBasemap.setSummary(mapBasemap.getEntry());
                }

                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });

        if (mapSdk.getValue().equals(OSM_BASEMAP_KEY)) {
            mapBasemap.setEntryValues(R.array.map_osm_basemap_selector_entry_values);
            mapBasemap.setEntries(R.array.map_osm_basemap_selector_entries);
        } else {
            mapBasemap.setEntryValues(R.array.map_google_basemap_selector_entry_values);
            mapBasemap.setEntries(R.array.map_google_basemap_selector_entries);
        }
        mapBasemap.setSummary(mapBasemap.getEntry());
        mapBasemap.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
    }

    private void initNavigationPrefs() {
        final ListPreference pref = listPref(KEY_NAVIGATION);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                    String entry = (String) ((ListPreference) preference).getEntries()[index];
                    preference.setSummary(entry);
                    return true;
                }
            });
        }
    }

    private void initUserAndPasswordPrefs() {
        final EditTextPreference userPref = (EditTextPreference) pref(KEY_USERNAME);

        if (userPref != null) {
            userPref.setOnPreferenceChangeListener(this);
            userPref.setSummary(userPref.getText());
            userPref.getEditText().setFilters(new InputFilter[]{getReturnFilter()});
        }

        final EditTextPreference passwordPref = (EditTextPreference) pref(KEY_PASSWORD);

        if (passwordPref != null) {
            passwordPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String pw = newValue.toString();

                    if (pw.length() > 0) {
                        passwordPref.setSummary("********");
                    } else {
                        passwordPref.setSummary("");
                    }
                    return true;
                }
            });
            if (passwordPref.getText() != null && passwordPref.getText().length() > 0) {
                passwordPref.setSummary("********");
            }
            passwordPref.getEditText().setFilters(new InputFilter[]{getReturnFilter()});
        }
    }

    private void initGoogleAccountPref() {
        final ListPreference pref = listPref(KEY_SELECTED_GOOGLE_ACCOUNT);

        if (pref == null)
            return;

        // get list of google accounts
        // ToDo: this code is duplicated somewhere. Fix the duplication.
        final List<String> accountEntries = new ArrayList<>();
        final List<String> accountValues = new ArrayList<>();
        final Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(
                "com.google");

        for (Account account : accounts) {
            accountEntries.add(account.name);
            accountValues.add(account.name);
        }
        accountEntries.add(getString(R.string.no_account));
        accountValues.add("");

        pref.setEntries(accountEntries.toArray(new String[accountEntries.size()]));
        pref.setEntryValues(accountValues.toArray(new String[accountValues.size()]));
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(
                        newValue.toString());
                String value =
                        (String) ((ListPreference) preference).getEntryValues()[index];
                preference.setSummary(value);
                return true;
            }
        });
        pref.setSummary(pref.getValue());
    }

    private void initProtocolPrefs(final boolean adminMode) {
        final ListPreference protocolPref = listPref(KEY_PROTOCOL);
        final Preference settingsPref = pref(KEY_PROTOCOL_SETTINGS);

        if (protocolPref == null || settingsPref == null)
            return;

        protocolPref.setSummary(protocolPref.getEntry());
        setProtocolIntent(adminMode, protocolPref.getValue(), settingsPref);

        protocolPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference lpref = (ListPreference) preference;
                String oldValue = lpref.getValue();
                int index = lpref.findIndexOfValue(newValue.toString());
                preference.setSummary(lpref.getEntries()[index]);

                Intent prefIntent = setProtocolIntent(adminMode,
                        lpref.getEntryValues()[index], settingsPref);

                if (!newValue.equals(oldValue)) {
                    startActivity(prefIntent);
                }

                return true;
            }
        });
    }

    private Intent setProtocolIntent(boolean adminMode, CharSequence value,
                                     Preference protocolSettings) {
        final Intent prefIntent;

        if (value.equals(getString(R.string.protocol_odk_default))) {
            setDefaultAggregatePaths();
            prefIntent = new Intent(this, AggregatePreferencesActivity.class);
        } else if (value.equals(
                getString(R.string.protocol_google_sheets))) {
            prefIntent = new Intent(this, GooglePreferencesActivity.class);
        } else {
            // other
            prefIntent = new Intent(this, OtherPreferencesActivity.class);
        }
        prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
        protocolSettings.setIntent(prefIntent);
        return prefIntent;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // the property manager should be re-assigned, as properties
        // may have changed.
        IPropertyManager mgr = new PropertyManager(this);
        FormController.initializeJavaRosa(mgr);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // has to go in onResume because it may get updated by
        // adminKey sub-preference screen
        // this just keeps the widgets in sync
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        ListPreference googleAccountPreference = listPref(KEY_SELECTED_GOOGLE_ACCOUNT);
        if (googleAccountPreference != null) {
            String account = sp.getString(KEY_SELECTED_GOOGLE_ACCOUNT, "");
            googleAccountPreference.setSummary(account);
            googleAccountPreference.setValue(account);
        }

        final EditTextPreference usernamePreference = (EditTextPreference) pref(KEY_USERNAME);
        if (usernamePreference != null) {
            String user = sp.getString(KEY_USERNAME, "");
            usernamePreference.setSummary(user);
            usernamePreference.setText(user);
        }

        final EditTextPreference passwordPreference = (EditTextPreference) pref(KEY_PASSWORD);
        if (passwordPreference != null) {
            String pw = sp.getString(KEY_PASSWORD, "");
            if (pw.length() > 0) {
                passwordPreference.setSummary("********");
                passwordPreference.setText(pw);
            }
        }
    }

    void setSplashPath(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.commit();

        PreferenceScreen splashPathPreference = (PreferenceScreen) pref(KEY_SPLASH_PATH);
        String summary = splashPathPreference.getSharedPreferences().getString(
                KEY_SPLASH_PATH, getString(R.string.default_splash_path));
        splashPathPreference.setSummary(summary);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case IMAGE_CHOOSER:

                // get gp of chosen file
                Uri selectedMedia = intent.getData();
                String sourceMediaPath = MediaUtils.getPathFromUri(this, selectedMedia,
                        Images.Media.DATA);

                // setting image path
                setSplashPath(sourceMediaPath);
                break;
        }
    }

    private void setDefaultAggregatePaths() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putString(KEY_FORMLIST_URL, getString(R.string.default_odk_formlist));
        editor.putString(KEY_SUBMISSION_URL, getString(R.string.default_odk_submission));
        editor.commit();
    }

    /**
     * Disallows carriage returns from user entry
     */
    protected InputFilter getReturnFilter() {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                       int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                        return "";
                    }
                }
                return null;
            }
        };
    }

    /**
     * Generic listener that sets the summary to the newly selected/entered value
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((CharSequence) newValue);
        return true;
    }
}
