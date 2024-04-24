package org.odk.collect.settings.enums;

import org.odk.collect.settings.R;

public enum FormUpdateMode implements StringIdEnum {

    MANUAL(R.string.form_update_mode_manual),
    PREVIOUSLY_DOWNLOADED_ONLY(R.string.form_update_mode_previously_downloaded),
    MATCH_EXACTLY(R.string.form_update_mode_match_exactly);

    private final int string;

    FormUpdateMode(int string) {
        this.string = string;
    }

    @Override
    public int getStringId() {
        return string;
    }
}
