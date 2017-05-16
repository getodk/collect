package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputFilter;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;


public class OtherPreferencesFragment extends AggregatePreferencesFragment implements Preference.OnPreferenceChangeListener {

    protected EditTextPreference submissionUrlPreference;
    protected EditTextPreference formListUrlPreference;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_preferences);

        formListUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_FORMLIST_URL);
        submissionUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_SUBMISSION_URL);

        InputFilter[] filters = {new ControlCharacterFilter(), new WhitespaceFilter()};

        serverUrlPreference.getEditText().setFilters(filters);

        formListUrlPreference.setOnPreferenceChangeListener(this);
        formListUrlPreference.setSummary(formListUrlPreference.getText());
        formListUrlPreference.getEditText().setFilters(filters);

        submissionUrlPreference.setOnPreferenceChangeListener(this);
        submissionUrlPreference.setSummary(submissionUrlPreference.getText());
        submissionUrlPreference.getEditText().setFilters(filters);

    }


    /**
     * Generic listener that sets the summary to the newly selected/entered
     * value
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((CharSequence) newValue);
        return true;
    }


}
