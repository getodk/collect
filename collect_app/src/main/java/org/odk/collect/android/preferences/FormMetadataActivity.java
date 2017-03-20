package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceActivity;

import org.odk.collect.android.R;

public class FormMetadataActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_metadata_preferences);
    }

}
