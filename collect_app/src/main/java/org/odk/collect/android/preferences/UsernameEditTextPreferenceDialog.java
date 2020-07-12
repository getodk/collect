package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.utilities.SoftKeyboardUtils;

public class UsernameEditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat implements View.OnTouchListener {

    private CustomEditTextPreference usernameEditTextPreference;

    public static UsernameEditTextPreferenceDialog newInstance(String key) {
        UsernameEditTextPreferenceDialog fragment = new UsernameEditTextPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        if (getPreference() instanceof CustomEditTextPreference) {
            usernameEditTextPreference = (CustomEditTextPreference) getPreference();
        }
        EditText editText = (EditText) view.findViewById(android.R.id.edit);
        editText.setFilters(new InputFilter[]{new ControlCharacterFilter()});
        usernameEditTextPreference.setOnPreferenceClickListener(preference -> {
            editText.requestFocus();
            return true;
        });

        super.onBindDialogView(view);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() >= (v.getWidth() - ((EditText) v)
                    .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                SoftKeyboardUtils.hideSoftKeyboard(v);
                return true;
            }
        }
        return false;
    }
}
