package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;

public class GoogleSheetsUrlEditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {

    public static GoogleSheetsUrlEditTextPreferenceDialog newInstance(String key) {
        GoogleSheetsUrlEditTextPreferenceDialog fragment = new GoogleSheetsUrlEditTextPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        CustomEditTextPreference googleSheetsUrlEditTextPreference = null;
        if (getPreference() instanceof CustomEditTextPreference) {
            googleSheetsUrlEditTextPreference = (CustomEditTextPreference) getPreference();
        }
        EditText editText = (EditText) view.findViewById(android.R.id.edit);
        editText.setFilters(new InputFilter[] {new ControlCharacterFilter(), new WhitespaceFilter() });

        if (googleSheetsUrlEditTextPreference != null) {
            googleSheetsUrlEditTextPreference.setOnPreferenceClickListener(preference -> {
                editText.requestFocus();
                return true;
            });
        }
        super.onBindDialogView(view);
    }
}
