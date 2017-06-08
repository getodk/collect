package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.PropertyManager;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_EMAIL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_PHONE_NUMBER;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_USERNAME;

/**
 * Created by shobhit on 24/4/17.
 */

public class FormMetadataFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PropertyManager pm = new PropertyManager(getActivity());
        initPrefFromProp(pm, prefs, PROPMGR_USERNAME, KEY_METADATA_USERNAME);
        initPrefFromProp(pm, prefs, PROPMGR_PHONE_NUMBER, KEY_METADATA_PHONENUMBER);
        initPrefFromProp(pm, prefs, PROPMGR_EMAIL, KEY_METADATA_EMAIL);
        initPrefFromProp(pm, prefs, PROPMGR_DEVICE_ID, null);
        initPrefFromProp(pm, prefs, PROPMGR_SUBSCRIBER_ID, null);
        initPrefFromProp(pm, prefs, PROPMGR_SIM_SERIAL, null);

    }

    /**
     * Initializes an EditTextPreference from a property.
     *
     * @param propertyManager   a PropertyManager
     * @param sharedPreferences shared preferences
     * @param propMgrName       the PropertyManager property name
     * @param differentPrefKey  the EditTextPreference key, null if the same as the propMgrName
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

    /**
     * Creates a change listener to update the UI, and save new values in shared preferences.
     */
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
