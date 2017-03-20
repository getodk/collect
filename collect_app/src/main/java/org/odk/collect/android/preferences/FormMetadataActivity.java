package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.PropertyManager;

import static org.odk.collect.android.logic.PropertyManager.DEVICE_ID_PROPERTY;
import static org.odk.collect.android.logic.PropertyManager.SIM_SERIAL_PROPERTY;
import static org.odk.collect.android.logic.PropertyManager.SUBSCRIBER_ID_PROPERTY;

public class FormMetadataActivity extends PreferenceActivity {
    public static final String KEY_DEVICE_ID        = "device_id";
    public static final String KEY_SUBSCRIBER_ID    = "subscriber_id";
    public static final String KEY_SIM_SERIAL_ID    = "sim_serial_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);
        PropertyManager mgr = new PropertyManager(this);
        setSummaryFromProperty(mgr, KEY_DEVICE_ID,      DEVICE_ID_PROPERTY);
        setSummaryFromProperty(mgr, KEY_SUBSCRIBER_ID,  SUBSCRIBER_ID_PROPERTY);
        setSummaryFromProperty(mgr, KEY_SIM_SERIAL_ID,  SIM_SERIAL_PROPERTY);
    }

    private void setSummaryFromProperty(PropertyManager mgr, String prefKey, String propKey) {
        EditTextPreference deviceIdPref = (EditTextPreference) findPreference(prefKey);
        String value = mgr.getSingularProperty(propKey);
        deviceIdPref.setSummary(value);
        deviceIdPref.setText(value);
    }
}
