package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.Validator;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_EMAIL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_PHONE_NUMBER;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_USERNAME;

public class FormMetadataFragment extends BasePreferenceFragment {
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.form_metadata_title);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        toolbar.setTitle(R.string.general_preferences);
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
                String newValueString = newValue.toString();

                if (KEY_METADATA_EMAIL.equals(key)) {
                    if (!newValueString.isEmpty() && !Validator.isEmailAddressValid(newValueString)) {
                        ToastUtils.showLongToast(R.string.invalid_email_address);
                        return false;
                    }
                }

                EditTextPreference changedTextPref = (EditTextPreference) preference;
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
