package org.odk.collect.android.preferences.utilities;

import android.content.Context;

import org.odk.collect.android.R;

public enum FormUpdateMode {

    MANUAL(R.string.form_update_mode_manual),
    PREVIOUSLY_DOWNLOADED_ONLY(R.string.form_update_mode_previously_downloaded),
    MATCH_EXACTLY(R.string.form_update_mode_match_exactly);

    private final int string;

    FormUpdateMode(int string) {
        this.string = string;
    }

    public static FormUpdateMode parse(Context context, String value) {
        if (MATCH_EXACTLY.getValue(context).equals(value)) {
            return MATCH_EXACTLY;
        } else if (PREVIOUSLY_DOWNLOADED_ONLY.getValue(context).equals(value)) {
            return PREVIOUSLY_DOWNLOADED_ONLY;
        } else {
            return MANUAL;
        }
    }

    public String getValue(Context context) {
        return context.getString(string);
    }
}
