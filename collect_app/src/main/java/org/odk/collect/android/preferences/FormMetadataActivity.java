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

public class FormMetadataActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);

        PropertyInitializer pi = new PropertyInitializer(new PropertyManager(this));
        pi.init(
                USERNAME_PROPERTY,
                PHONE_NUMBER_PROPERTY,
                EMAIL_PROPERTY,
                DEVICE_ID_PROPERTY,
                SUBSCRIBER_ID_PROPERTY,
                SIM_SERIAL_PROPERTY)
        ;
    }

    private class PropertyInitializer {
        private final PropertyManager propertyManager;
        private final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(FormMetadataActivity.this);

        PropertyInitializer(PropertyManager propertyManager) {
            this.propertyManager = propertyManager;
        }

        /**
         * For each key in keys for which the PropertyManager has a value, sets up the
         * EditTextPreference having that key, and creates a change listener.
         * @param keys one or more PropertyManager/EditTextPreference keys
         */
        private void init(final String... keys) {
            for (final String key : keys) {
                String propVal = propertyManager.getSingularProperty(key);
                if (propVal != null) {
                    EditTextPreference textPref = (EditTextPreference) findPreference(key);
                    textPref.setSummary(propVal);
                    textPref.setText(propVal);
                    textPref.setOnPreferenceChangeListener(createChangeListener(key));
                }
            }
        }

        /** Creates a change listener to update the UI, and save new values in shared preferences. */
        private Preference.OnPreferenceChangeListener createChangeListener(final String key) {
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
}
