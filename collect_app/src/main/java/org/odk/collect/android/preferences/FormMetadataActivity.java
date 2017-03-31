package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.PropertyManager;
import static org.odk.collect.android.logic.PropertyManager.*;
import static org.odk.collect.android.preferences.PreferenceKeys.*;

public class FormMetadataActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        migrateOnce(prefs);
        PropertyManager pm = new PropertyManager(this);
        initPrefFromProp(pm, prefs, PROPMGR_USERNAME,       KEY_METADATA_USERNAME);
        initPrefFromProp(pm, prefs, PROPMGR_PHONE_NUMBER,   KEY_METADATA_PHONENUMBER);
        initPrefFromProp(pm, prefs, PROPMGR_EMAIL,          KEY_METADATA_EMAIL);
        initPrefFromProp(pm, prefs, PROPMGR_DEVICE_ID,      null);
        initPrefFromProp(pm, prefs, PROPMGR_SUBSCRIBER_ID,  null);
        initPrefFromProp(pm, prefs, PROPMGR_SIM_SERIAL,     null);
    }

    void migrateOnce(SharedPreferences sharedPreferences) {
        boolean migrationAlreadyDone = sharedPreferences.getBoolean(KEY_METADATA_MIGRATED, false);

        if (! migrationAlreadyDone) {
            String[][] fromToKeyPairs = {
                    {KEY_USERNAME,                  KEY_METADATA_USERNAME},
                    {KEY_SELECTED_GOOGLE_ACCOUNT,   KEY_METADATA_EMAIL}
            };

            for (String[] pair : fromToKeyPairs) {
                String migratingValue = sharedPreferences.getString(pair[0], "").trim();
                if (! migratingValue.isEmpty()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(pair[1], migratingValue);
                    editor.apply();
                }
            }

            // Save that we’ve migrated the values
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_METADATA_MIGRATED, true);
            editor.apply();
        }
    }

    /**
     * Initializes an EditTextPreference from a property.
     * @param propertyManager a PropertyManager
     * @param sharedPreferences shared preferences
     * @param propMgrName the PropertyManager property name
     * @param differentPrefKey the EditTextPreference key, null if the same as the propMgrName
     */
    private void initPrefFromProp(PropertyManager propertyManager,
                                  SharedPreferences sharedPreferences, String propMgrName,
                                  String differentPrefKey) {
        String propVal = propertyManager.getSingularProperty(propMgrName);
        String prefKey = differentPrefKey == null ? propMgrName : differentPrefKey;
        EditTextPreference textPref = (EditTextPreference) findPreference(prefKey);
        if (propVal != null) {
            textPref.setSummary(propVal);
            textPref.setText(propVal);
        }
        if (textPref.isSelectable()) {
            textPref.setOnPreferenceChangeListener(createChangeListener(sharedPreferences, prefKey));
        }
    }

    /** Creates a change listener to update the UI, and save new values in shared preferences. */
    private Preference.OnPreferenceChangeListener createChangeListener(final SharedPreferences sharedPreferences, final String key) {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                EditTextPreference changedTextPref = (EditTextPreference) preference;
                String newValueString = newValue.toString();
                changedTextPref.setSummary(newValueString);
                changedTextPref.setText(newValueString);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, newValueString);
                editor.apply();
                return true;
            }
        };
    }
}
