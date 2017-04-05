package org.odk.collect.android.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.odk.collect.android.R;

import static org.odk.collect.android.preferences.PreferenceKeys.ARRAY_INDEX_GOOGLE_MAPS;
import static org.odk.collect.android.preferences.PreferenceKeys.GOOGLE_MAPS_BASEMAP_DEFAULT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_ANALYTICS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORM_METADATA;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_BASEMAP;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_SDK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_NAVIGATION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL_SETTINGS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SPLASH_PATH;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.OSM_BASEMAP_KEY;
import static org.odk.collect.android.preferences.PreferenceKeys.OSM_MAPS_BASEMAP_DEFAULT;


public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
    private static final String TAG = "PreferencesFragment";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final boolean adminMode = getActivity().getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        removeAllDisabledPrefs();

        initProtocolPrefs(adminMode);
        initFormMetadata();
        initNavigationPrefs();
        initConstraintBehaviorPref();
        initFontSizePref();
        initAnalyticsPref();
        initSplashPrefs();
        initMapPrefs();
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // has to go in onResume because it may get updated by
        // a sub-preference screen
        // this just keeps the widgets in sync
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference googleAccountPreference = (ListPreference) findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);
        if (googleAccountPreference != null) {
            String account = sp.getString(KEY_SELECTED_GOOGLE_ACCOUNT, "");
            googleAccountPreference.setSummary(account);
            googleAccountPreference.setValue(account);
        }

        final EditTextPreference usernamePreference = (EditTextPreference) findPreference(KEY_USERNAME);
        if (usernamePreference != null) {
            String user = sp.getString(KEY_USERNAME, "");
            usernamePreference.setSummary(user);
            usernamePreference.setText(user);
        }

        final EditTextPreference passwordPreference = (EditTextPreference) findPreference(KEY_PASSWORD);
        if (passwordPreference != null) {
            String pw = sp.getString(KEY_PASSWORD, "");
            if (pw.length() > 0) {
                passwordPreference.setSummary("********");
                passwordPreference.setText(pw);
            }
        }
    }


    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover((PreferencesActivity) getActivity(), this);
        preferencesRemover.remove(AdminKeys.adminToGeneral);
        preferencesRemover.removeEmptyCategories();
    }

    private void initAnalyticsPref() {
        final CheckBoxPreference analyticsPreference = (CheckBoxPreference) findPreference(KEY_ANALYTICS);

        if (analyticsPreference != null) {
            analyticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getActivity().getApplicationContext());
                    googleAnalytics.setAppOptOut(!analyticsPreference.isChecked());
                    return true;
                }
            });
        }
    }

    private void initSplashPrefs() {
        final PreferenceScreen pref = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);

        if (pref != null) {
            pref.setOnPreferenceClickListener(new SplashClickListener((PreferencesActivity) getActivity(), pref));
            pref.setSummary(pref.getSharedPreferences().getString(
                    KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
        }
    }

    private void initFontSizePref() {
        final ListPreference pref = (ListPreference) findPreference(KEY_FONT_SIZE);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

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
        final ListPreference pref = (ListPreference) findPreference(KEY_CONSTRAINT_BEHAVIOR);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {

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
        final ListPreference mapSdk = (ListPreference) findPreference(KEY_MAP_SDK);
        final ListPreference mapBasemap = (ListPreference) findPreference(KEY_MAP_BASEMAP);

        if (mapSdk == null || mapBasemap == null)
            return;

        mapSdk.setSummary(mapSdk.getEntry());
        mapSdk.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

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
        mapBasemap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
        final ListPreference pref = (ListPreference) findPreference(KEY_NAVIGATION);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

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

    private void initProtocolPrefs(final boolean adminMode) {
        final ListPreference protocolPref = (ListPreference) findPreference(KEY_PROTOCOL);
        final Preference settingsPref = findPreference(KEY_PROTOCOL_SETTINGS);

        if (protocolPref == null || settingsPref == null)
            return;

        protocolPref.setSummary(protocolPref.getEntry());
        setProtocolIntent(adminMode, protocolPref.getValue(), settingsPref);

        protocolPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

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

    private void initFormMetadata() {
        final Preference pref = findPreference(KEY_FORM_METADATA);

        if (pref != null) {
            final Intent intent = new Intent(getActivity(), FormMetadataActivity.class);
            pref.setIntent(intent);
        }
    }

    private Intent setProtocolIntent(boolean adminMode, CharSequence value,
                                     Preference protocolSettings) {
        final Intent prefIntent;

        if (value.equals(getString(R.string.protocol_odk_default))) {
            setDefaultAggregatePaths();
            prefIntent = new Intent(getActivity(), AggregatePreferencesActivity.class);
        } else if (value.equals(getString(R.string.protocol_google_sheets))) {
            prefIntent = new Intent(getActivity(), GooglePreferencesActivity.class);
        } else {
            // other
            prefIntent = new Intent(getActivity(), OtherPreferencesActivity.class);
        }
        prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
        protocolSettings.setIntent(prefIntent);
        return prefIntent;
    }

    private void setDefaultAggregatePaths() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_FORMLIST_URL, getString(R.string.default_odk_formlist));
        editor.putString(KEY_SUBMISSION_URL, getString(R.string.default_odk_submission));
        editor.apply();
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
