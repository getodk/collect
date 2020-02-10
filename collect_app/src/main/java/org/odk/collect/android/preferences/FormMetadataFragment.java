package org.odk.collect.android.preferences;

import android.app.Activity;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.Validator;

import javax.inject.Inject;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_PHONE_NUMBER;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_INSTALL_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;

public class FormMetadataFragment extends PreferenceFragmentCompat {

    @Inject
    InstallIDProvider installIDProvider;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DaggerUtils.getComponent(activity).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.form_metadata_preferences, rootKey);
        ((CollectAbstractActivity) getActivity()).initToolbar(getPreferenceScreen().getTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        findPreference(KEY_METADATA_EMAIL).setOnPreferenceChangeListener((preference, newValue) -> {
            String newValueString = newValue.toString();
            if (!newValueString.isEmpty() && !Validator.isEmailAddressValid(newValueString)) {
                ToastUtils.showLongToast(R.string.invalid_email_address);
                return false;
            }

            return true;
        });

        findPreference(KEY_INSTALL_ID).setSummaryProvider(preference -> installIDProvider.getInstallID());
    }

    private void initDangerousPrefs() {
        PropertyManager pm = new PropertyManager(getActivity());
        initPrefFromProp(pm, PROPMGR_PHONE_NUMBER, KEY_METADATA_PHONENUMBER);
        initPrefFromProp(pm, PROPMGR_DEVICE_ID, PROPMGR_DEVICE_ID);
        initPrefFromProp(pm, PROPMGR_SUBSCRIBER_ID, PROPMGR_SUBSCRIBER_ID);
        initPrefFromProp(pm, PROPMGR_SIM_SERIAL, PROPMGR_SIM_SERIAL);
    }

    /**
     * Initializes an EditTextPreference from a property.
     *  @param propertyManager   a PropertyManager
     * @param propMgrName       the PropertyManager property name
     * @param prefKey           the EditTextPreference key
     */
    private void initPrefFromProp(PropertyManager propertyManager,
                                  String propMgrName,
                                  String prefKey) {
        String propVal = propertyManager.getSingularProperty(propMgrName);
        EditTextPreference textPref = findPreference(prefKey);

        if (propVal != null) {
            textPref.setText(propVal);
        }
    }

}
