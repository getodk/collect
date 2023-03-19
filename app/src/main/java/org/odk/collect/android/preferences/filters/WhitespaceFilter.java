package org.odk.collect.android.preferences.filters;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Rejects edits that contain whitespace.
 */
public class WhitespaceFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (Character.isWhitespace(source.charAt(i))) {
                return "";
            }
        }
        return null;
    }
}
