package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.Validator;

import javax.inject.Inject;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_EMAIL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_PHONE_NUMBER;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_INSTALL_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_USERNAME;

public class FormMetadataFragment extends BasePreferenceFragment {

    @Inject
    public InstallIDProvider installIDProvider;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DaggerUtils.getComponent(activity).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);

        initNormalPrefs();

        if (savedInstanceState == null) {
            new PermissionUtils().requestReadPhoneStatePermission(getActivity(), true, new PermissionListener() {
                @Override
                public void granted() {
                    initDangerousPrefs();
                }

                @Override
                public void denied() {
                }
            });
        }
    }

    private void initNormalPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PropertyManager pm = new PropertyManager(getActivity());
        initPrefFromProp(pm, prefs, PROPMGR_USERNAME, KEY_METADATA_USERNAME);
        initPrefFromProp(pm, prefs, PROPMGR_EMAIL, KEY_METADATA_EMAIL);
    }

    private void initDangerousPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PropertyManager pm = new PropertyManager(getActivity());
        initPrefFromProp(pm, prefs, PROPMGR_PHONE_NUMBER, KEY_METADATA_PHONENUMBER);
        initPrefFromProp(pm, prefs, PROPMGR_DEVICE_ID, PROPMGR_DEVICE_ID);
        initPrefFromProp(pm, prefs, PROPMGR_SUBSCRIBER_ID, PROPMGR_SUBSCRIBER_ID);
        initPrefFromProp(pm, prefs, PROPMGR_SIM_SERIAL, PROPMGR_SIM_SERIAL);

        findPreference(KEY_INSTALL_ID).setSummary(installIDProvider.getInstallID());
    }

    /**
     * Initializes an EditTextPreference from a property.
     *
     * @param propertyManager   a PropertyManager
     * @param sharedPreferences shared preferences
     * @param propMgrName       the PropertyManager property name
     * @param prefKey           the EditTextPreference key
     */
    private void initPrefFromProp(PropertyManager propertyManager,
                                  SharedPreferences sharedPreferences,
                                  String propMgrName,
                                  String prefKey) {
        String propVal = propertyManager.getSingularProperty(propMgrName);
        EditTextPreference textPref = (EditTextPreference) findPreference(prefKey);

        textPref.setOnPreferenceClickListener(preference -> {
            textPref.getEditText().requestFocus();
            return true;
        });

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
        return (preference, newValue) -> {
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
        };
    }
}
