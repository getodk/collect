package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.PropertyManager;

import static org.odk.collect.android.logic.PropertyManager.*;

public class FormMetadataActivity extends PreferenceActivity {
    public static final String KEY_USERNAME         = "username";
    public static final String KEY_PHONE_NUMBER     = "phone_number";
    public static final String KEY_EMAIL            = "email";
    public static final String KEY_DEVICE_ID        = "device_id";
    public static final String KEY_SUBSCRIBER_ID    = "subscriber_id";
    public static final String KEY_SIM_SERIAL_ID    = "sim_serial_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);
        PropertyInitializer pi = new PropertyInitializer(new PropertyManager(this));
        pi.init(KEY_USERNAME,       USERNAME_PROPERTY);
        pi.init(KEY_PHONE_NUMBER,   PHONE_NUMBER_PROPERTY);
        pi.init(KEY_EMAIL,          EMAIL_PROPERTY);
        pi.init(KEY_DEVICE_ID,      DEVICE_ID_PROPERTY);
        pi.init(KEY_SUBSCRIBER_ID,  SUBSCRIBER_ID_PROPERTY);
        pi.init(KEY_SIM_SERIAL_ID,  SIM_SERIAL_PROPERTY);
    }

    private class PropertyInitializer {
        private final PropertyManager mgr;

        PropertyInitializer(PropertyManager mgr) {
            this.mgr = mgr;
        }

        void init(String prefKey, final String propKey) {
            String propVal = mgr.getSingularProperty(propKey);
            if (propVal != null) {
                final EditTextPreference pref = (EditTextPreference) findPreference(prefKey);
                pref.setSummary(propVal);
                pref.setText(propVal);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        System.out.println(propKey + " changed");
                        String newValueString = newValue.toString();
                        pref.setSummary(newValueString);
                        pref.setText(newValueString);
                        mgr.setProperty(propKey, newValueString);
                        return true;
                    }
                });
            }
        }
    }
}
