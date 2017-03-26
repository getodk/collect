package org.odk.collect.android.preferences.filters;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Rejects edits that contain control characters, including linefeed and carriage return.
 */
public class ControlCharacterFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                return "";
            }
        }
        return null;
    }
}
