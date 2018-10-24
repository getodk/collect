package org.odk.collect.android.preferences;

import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.odk.collect.android.R;

class AggregatePreferencesAdder {

    private final PreferenceFragment fragment;

    AggregatePreferencesAdder(PreferenceFragment fragment) {
        this.fragment = fragment;
    }

    public boolean add() {
        try {
            fragment.addPreferencesFromResource(R.xml.aggregate_preferences);
            return true;
        } catch (ClassCastException e) {
            Toast.makeText(fragment.getActivity(), R.string.corrupt_imported_preferences_error, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
