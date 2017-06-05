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

import com.google.android.gms.analytics.GoogleAnalytics;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.utilities.LocaleHelper;

import java.util.ArrayList;
import java.util.TreeMap;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.ARRAY_INDEX_GOOGLE_MAPS;
import static org.odk.collect.android.preferences.PreferenceKeys.GOOGLE_MAPS_BASEMAP_DEFAULT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_ANALYTICS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORM_METADATA;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_BASEMAP;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_SDK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_NAVIGATION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SPLASH_PATH;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.OSM_BASEMAP_KEY;
import static org.odk.collect.android.preferences.PreferenceKeys.OSM_MAPS_BASEMAP_DEFAULT;


public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        removeAllDisabledPrefs();

        initPlatformSettings();
        initFormMetadata();
        initNavigationPrefs();
        initConstraintBehaviorPref();
        initFontSizePref();
        initLanguagePrefs();
        initAnalyticsPref();
        initSplashPrefs();
        initMapPrefs();
        initAutoSendPrefs();
    }

    private void initPlatformSettings() {
        final Preference protocol = findPreference(KEY_PROTOCOL);

        if (protocol == null) {
            return;
        }

        protocol.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ServerPreferencesActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void initAutoSendPrefs() {
        final ListPreference autosend = (ListPreference) findPreference(KEY_AUTOSEND);

        if (autosend == null) {
            return;
        }

        autosend.setSummary(autosend.getEntry());
        autosend.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        Timber.d("onResume");
        super.onResume();

        // has to go in onResume because it may get updated by
        // a sub-preference screen
        // this just keeps the widgets in sync
        GeneralSharedPreferences sp = GeneralSharedPreferences.getInstance();

        ListPreference googleAccountPreference = (ListPreference) findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);
        if (googleAccountPreference != null) {
            String account = (String) sp.get(KEY_SELECTED_GOOGLE_ACCOUNT);
            googleAccountPreference.setSummary(account);
            googleAccountPreference.setValue(account);
        }

        final EditTextPreference usernamePreference = (EditTextPreference) findPreference(KEY_USERNAME);
        if (usernamePreference != null) {
            String user = (String) sp.get(KEY_USERNAME);
            usernamePreference.setSummary(user);
            usernamePreference.setText(user);
        }

        final EditTextPreference passwordPreference = (EditTextPreference) findPreference(KEY_PASSWORD);
        if (passwordPreference != null) {
            String pw = (String) sp.get(KEY_PASSWORD);
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

    private void initLanguagePrefs() {
        final ListPreference pref = (ListPreference) findPreference(KEY_APP_LANGUAGE);

        if (pref != null) {
            final LocaleHelper localeHelper = new LocaleHelper();
            TreeMap<String, String> languageList = localeHelper.getEntryListValues();
            int length = languageList.size() + 1;
            ArrayList<String> entryValues = new ArrayList<>();
            entryValues.add(0, "");
            entryValues.addAll(languageList.values());
            pref.setEntryValues(entryValues.toArray(new String[length]));
            ArrayList<String> entries = new ArrayList<>();
            entries.add(0, getActivity().getResources()
                    .getString(R.string.use_phone_language));
            entries.addAll(languageList.keySet());
            pref.setEntries(entries.toArray(new String[length]));
            if (pref.getValue() == null) {
                //set Default value to "Use phone locale"
                pref.setValueIndex(0);
            }
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                    String entry = (String) ((ListPreference) preference).getEntries()[index];
                    preference.setSummary(entry);

                    SharedPreferences.Editor edit = PreferenceManager
                            .getDefaultSharedPreferences(getActivity()).edit();
                    edit.putString(KEY_APP_LANGUAGE, newValue.toString());
                    edit.apply();

                    localeHelper.updateLocale(getActivity());

                    Intent intent = new Intent(getActivity().getBaseContext(), MainMenuActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finishAffinity();
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

        if (mapSdk == null || mapBasemap == null) {
            return;
        }

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

    private void initFormMetadata() {
        final Preference pref = findPreference(KEY_FORM_METADATA);

        if (pref != null) {
            final Intent intent = new Intent(getActivity(), FormMetadataActivity.class);
            pref.setIntent(intent);
        }
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
