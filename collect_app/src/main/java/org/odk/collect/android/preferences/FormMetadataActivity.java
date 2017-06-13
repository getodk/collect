package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class FormMetadataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new FormMetadataFragment())
                .commit();
    }
}
