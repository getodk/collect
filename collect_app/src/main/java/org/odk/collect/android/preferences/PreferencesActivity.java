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
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
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

import java.util.ArrayList;

/**
 * Handles general preferences.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com;
 *         constraint behavior option)
 */
public class PreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
    protected static final int IMAGE_CHOOSER = 0;

    private PreferenceScreen mSplashPathPreference;
    private ListPreference mSelectedGoogleAccountPreference;
    private ListPreference mNavigationPreference;
    private ListPreference mConstraintBehaviorPreference;
    private CheckBoxPreference mAutosendWifiPreference;
    private CheckBoxPreference mAutosendNetworkPreference;
    private ListPreference mProtocolPreference;
    private PreferenceScreen mProtocolSettings;
    protected EditTextPreference mUsernamePreference;
    protected EditTextPreference mPasswordPreference;

    protected ListPreference mMapSdk;
    protected ListPreference mMapBasemap;

    private CheckBoxPreference mAnalyticsPreference;

    /** Encapsulate the findPreference deprecation warning */
    private Preference pref(String key) {
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

        // not super safe, but we're just putting in this mode to help
        // administrate
        // would require code to access it
        final boolean adminMode = getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        SharedPreferences adminPreferences = getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        // assign all the preferences in advance because changing one often
        // affects another
        // also avoids npe
        PreferenceCategory autosendCategory = (PreferenceCategory) pref(
                getString(R.string.autosend));
        mAutosendWifiPreference = (CheckBoxPreference) pref(KEY_AUTOSEND_WIFI);
        mAutosendNetworkPreference = (CheckBoxPreference) pref(KEY_AUTOSEND_NETWORK);
        PreferenceCategory serverCategory = (PreferenceCategory) pref(
                getString(R.string.server_preferences));

        mProtocolPreference = listPref(KEY_PROTOCOL);

        mSelectedGoogleAccountPreference = listPref(KEY_SELECTED_GOOGLE_ACCOUNT);
        PreferenceCategory clientCategory = (PreferenceCategory) pref(
                getString(R.string.client));
        PreferenceCategory mapCategory = (PreferenceCategory) pref(
                getString(R.string.map_preferences));
        mNavigationPreference = listPref(KEY_NAVIGATION);
        Preference defaultFinalized = pref(KEY_COMPLETED_DEFAULT);
        Preference deleteAfterSend = pref(KEY_DELETE_AFTER_SEND);
        mSplashPathPreference = (PreferenceScreen) pref(KEY_SPLASH_PATH);
        mConstraintBehaviorPreference = listPref(KEY_CONSTRAINT_BEHAVIOR);

        mUsernamePreference = (EditTextPreference) pref(KEY_USERNAME);
        mPasswordPreference = (EditTextPreference) pref(KEY_PASSWORD);

        mProtocolSettings = (PreferenceScreen) pref(KEY_PROTOCOL_SETTINGS);

        mMapSdk = listPref(KEY_MAP_SDK);
        mMapBasemap = listPref(KEY_MAP_BASEMAP);

        boolean autosendWifiAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_AUTOSEND_WIFI, true);
        if (!(autosendWifiAvailable || adminMode)) {
            autosendCategory.removePreference(mAutosendWifiPreference);
        }

        boolean autosendNetworkAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_AUTOSEND_NETWORK, true);
        if (!(autosendNetworkAvailable || adminMode)) {
            autosendCategory.removePreference(mAutosendNetworkPreference);
        }

        if (!(autosendNetworkAvailable || autosendWifiAvailable || adminMode)) {
            getPreferenceScreen().removePreference(autosendCategory);
        }

        mProtocolPreference = listPref(KEY_PROTOCOL);
        mProtocolPreference.setSummary(mProtocolPreference.getEntry());
        Intent prefIntent = null;

        if (mProtocolPreference.getValue().equals(getString(R.string.protocol_odk_default))) {
            setDefaultAggregatePaths();
            prefIntent = new Intent(this, AggregatePreferencesActivity.class);
        } else if (mProtocolPreference.getValue().equals(
                getString(R.string.protocol_google_sheets))) {
            prefIntent = new Intent(this, GooglePreferencesActivity.class);
        } else {
            // other
            prefIntent = new Intent(this, OtherPreferencesActivity.class);
        }
        prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
        mProtocolSettings.setIntent(prefIntent);

        mProtocolPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String oldValue = ((ListPreference) preference).getValue();
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                String value = (String) ((ListPreference) preference).getEntryValues()[index];
                preference.setSummary(entry);

                Intent prefIntent = null;
                if (value.equals(getString(R.string.protocol_odk_default))) {
                    setDefaultAggregatePaths();
                    prefIntent = new Intent(PreferencesActivity.this,
                            AggregatePreferencesActivity.class);
                } else if (value.equals(getString(R.string.protocol_google_sheets))) {
                    prefIntent = new Intent(PreferencesActivity.this,
                            GooglePreferencesActivity.class);
                } else {
                    // other
                    prefIntent = new Intent(PreferencesActivity.this,
                            OtherPreferencesActivity.class);
                }
                prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
                mProtocolSettings.setIntent(prefIntent);

                if (!((String) newValue).equals(oldValue)) {
                    startActivity(prefIntent);
                }

                return true;
            }
        });

        boolean changeProtocol = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_SERVER, true);
        if (!(changeProtocol || adminMode)) {
            serverCategory.removePreference(mProtocolPreference);
        }
        boolean changeProtocolSettings = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_PROTOCOL_SETTINGS, true);
        if (!(changeProtocolSettings || adminMode)) {
            serverCategory.removePreference(mProtocolSettings);
        }

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

        mSelectedGoogleAccountPreference.setEntries(accountEntries.toArray(new String[accountEntries
                .size()]));
        mSelectedGoogleAccountPreference.setEntryValues(
                accountValues.toArray(new String[accountValues
                        .size()]));
        mSelectedGoogleAccountPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int index = ((ListPreference) preference).findIndexOfValue(
                                newValue.toString());
                        String value =
                                (String) ((ListPreference) preference).getEntryValues()[index];
                        ((ListPreference) preference).setSummary(value);
                        return true;
                    }
                });
        mSelectedGoogleAccountPreference.setSummary(mSelectedGoogleAccountPreference.getValue());

        boolean googleAccountAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_GOOGLE_ACCOUNT, true);
        if (!(googleAccountAvailable || adminMode)) {
            serverCategory.removePreference(mSelectedGoogleAccountPreference);
        }

        mUsernamePreference.setOnPreferenceChangeListener(this);
        mUsernamePreference.setSummary(mUsernamePreference.getText());
        mUsernamePreference.getEditText().setFilters(new InputFilter[]{getReturnFilter()});

        boolean usernameAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_USERNAME, true);
        if (!(usernameAvailable || adminMode)) {
            serverCategory.removePreference(mUsernamePreference);
        }

        mPasswordPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String pw = newValue.toString();

                if (pw.length() > 0) {
                    mPasswordPreference.setSummary("********");
                } else {
                    mPasswordPreference.setSummary("");
                }
                return true;
            }
        });
        if (mPasswordPreference.getText() != null && mPasswordPreference.getText().length() > 0) {
            mPasswordPreference.setSummary("********");
        }
        mPasswordPreference.getEditText().setFilters(new InputFilter[]{getReturnFilter()});

        boolean passwordAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_PASSWORD, true);
        if (!(passwordAvailable || adminMode)) {
            serverCategory.removePreference(mPasswordPreference);
        }

        boolean navigationAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_NAVIGATION, true);
        mNavigationPreference.setSummary(mNavigationPreference.getEntry());
        mNavigationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
        if (!(navigationAvailable || adminMode)) {
            clientCategory.removePreference(mNavigationPreference);
        }

        boolean constraintBehaviorAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CONSTRAINT_BEHAVIOR, true);
        mConstraintBehaviorPreference.setSummary(mConstraintBehaviorPreference.getEntry());
        mConstraintBehaviorPreference.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int index = ((ListPreference) preference).findIndexOfValue(
                                newValue.toString());
                        String entry = (String) ((ListPreference) preference).getEntries()[index];
                        ((ListPreference) preference).setSummary(entry);
                        return true;
                    }
                });
        if (!(constraintBehaviorAvailable || adminMode)) {
            clientCategory.removePreference(mConstraintBehaviorPreference);
        }

        boolean fontAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_FONT_SIZE, true);
        final ListPreference fontSizePreference = listPref(KEY_FONT_SIZE);
        fontSizePreference.setSummary(fontSizePreference.getEntry());
        fontSizePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
        if (!(fontAvailable || adminMode)) {
            clientCategory.removePreference(fontSizePreference);
        }

        boolean defaultAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_DEFAULT_TO_FINALIZED, true);

        if (!(defaultAvailable || adminMode)) {
            clientCategory.removePreference(defaultFinalized);
        }

        boolean deleteAfterAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_DELETE_AFTER_SEND, true);
        if (!(deleteAfterAvailable || adminMode)) {
            clientCategory.removePreference(deleteAfterSend);
        }

        boolean resolutionAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_HIGH_RESOLUTION, true);

        Preference highResolution = pref(KEY_HIGH_RESOLUTION);
        if (!(resolutionAvailable || adminMode)) {
            clientCategory.removePreference(highResolution);
        }

        PreferenceCategory analyticsCategory = (PreferenceCategory) pref(
                getString(R.string.analytics_preferences));
        mAnalyticsPreference = (CheckBoxPreference) pref(KEY_ANALYTICS);

        boolean analyticsAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_ANALYTICS, true);
        if (!(analyticsAvailable || adminMode)) {
            analyticsCategory.removePreference(mAnalyticsPreference);
            getPreferenceScreen().removePreference(analyticsCategory);
        }

        mAnalyticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
                googleAnalytics.setAppOptOut(!mAnalyticsPreference.isChecked());
                return true;
            }
        });

        mSplashPathPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            private void launchImageChooser() {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, PreferencesActivity.IMAGE_CHOOSER);
            }

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // if you have a value, you can clear it or select new.
                CharSequence cs = mSplashPathPreference.getSummary();
                if (cs != null && cs.toString().contains("/")) {

                    final CharSequence[] items = {getString(R.string.select_another_image),
                            getString(R.string.use_odk_default)};

                    AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
                    builder.setTitle(getString(R.string.change_splash_path));
                    builder.setNeutralButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (items[item].equals(getString(R.string.select_another_image))) {
                                launchImageChooser();
                            } else {
                                setSplashPath(getString(R.string.default_splash_path));
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                } else {
                    launchImageChooser();
                }

                return true;
            }
        });

        mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
                KEY_SPLASH_PATH, getString(R.string.default_splash_path)));

        boolean showSplashAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_SHOW_SPLASH_SCREEN, true);

        CheckBoxPreference showSplashPreference = (CheckBoxPreference) pref(KEY_SHOW_SPLASH);

        if (!(showSplashAvailable || adminMode)) {
            clientCategory.removePreference(showSplashPreference);
            clientCategory.removePreference(mSplashPathPreference);
        }

        if (!(fontAvailable || defaultAvailable || showSplashAvailable || navigationAvailable
                || adminMode || resolutionAvailable)) {
            getPreferenceScreen().removePreference(clientCategory);
        }

        // MAP SPECIFIC
        boolean mMapSdkAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_SHOW_MAP_SDK, true);
        mMapSdk.setSummary(mMapSdk.getEntry());
        mMapSdk.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                if (index == ARRAY_INDEX_GOOGLE_MAPS) {
                    mMapBasemap.setEntryValues(R.array.map_google_basemap_selector_entry_values);
                    mMapBasemap.setEntries(R.array.map_google_basemap_selector_entries);
                    mMapBasemap.setValue(GOOGLE_MAPS_BASEMAP_DEFAULT);
                    mMapBasemap.setSummary(mMapBasemap.getEntry());
                } else {
                    // Else its OSM Maps
                    mMapBasemap.setEntryValues(R.array.map_osm_basemap_selector_entry_values);
                    mMapBasemap.setEntries(R.array.map_osm_basemap_selector_entries);
                    mMapBasemap.setValue(OSM_MAPS_BASEMAP_DEFAULT);
                    mMapBasemap.setSummary(mMapBasemap.getEntry());
                }

                String entry = (String) ((ListPreference) preference).getEntries()[index];
                ((ListPreference) preference).setSummary(entry);
                return true;
            }
        });

        if (!(mMapSdkAvailable || adminMode)) {
            mapCategory.removePreference(mMapSdk);
        }

        boolean mMapBasemapAvailable = adminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_SHOW_MAP_BASEMAP, true);

        if (mMapSdk.getValue().equals(OSM_BASEMAP_KEY)) {
            mMapBasemap.setEntryValues(R.array.map_osm_basemap_selector_entry_values);
            mMapBasemap.setEntries(R.array.map_osm_basemap_selector_entries);
        } else {
            mMapBasemap.setEntryValues(R.array.map_google_basemap_selector_entry_values);
            mMapBasemap.setEntries(R.array.map_google_basemap_selector_entries);
        }
        mMapBasemap.setSummary(mMapBasemap.getEntry());
        mMapBasemap.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                ((ListPreference) preference).setSummary(entry);
                return true;
            }
        });
        if (!(mMapBasemapAvailable || adminMode)) {
            mapCategory.removePreference(mMapBasemap);
        }
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
        // a sub-preference screen
        // this just keeps the widgets in sync
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String account = sp.getString(KEY_SELECTED_GOOGLE_ACCOUNT, "");
        mSelectedGoogleAccountPreference.setSummary(account);
        mSelectedGoogleAccountPreference.setValue(account);

        String user = sp.getString(KEY_USERNAME, "");
        String pw = sp.getString(KEY_PASSWORD, "");
        mUsernamePreference.setSummary(user);
        mUsernamePreference.setText(user);
        if (pw != null && pw.length() > 0) {
            mPasswordPreference.setSummary("********");
            mPasswordPreference.setText(pw);
        }

    }

    private void setSplashPath(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.commit();

        mSplashPathPreference = (PreferenceScreen) pref(KEY_SPLASH_PATH);
        mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
                KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
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
        InputFilter returnFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart,
                    int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                        return "";
                    }
                }
                return null;
            }
        };
        return returnFilter;
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
