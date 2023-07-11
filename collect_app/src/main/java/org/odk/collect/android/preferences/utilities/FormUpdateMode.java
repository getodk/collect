package org.odk.collect.android.preferences.utilities;

import android.content.Context;

public enum FormUpdateMode {

    MANUAL(org.odk.collect.strings.R.string.form_update_mode_manual),
    PREVIOUSLY_DOWNLOADED_ONLY(org.odk.collect.strings.R.string.form_update_mode_previously_downloaded),
    MATCH_EXACTLY(org.odk.collect.strings.R.string.form_update_mode_match_exactly);

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
