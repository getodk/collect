package org.odk.collect.android.formentry.questions;

import org.odk.collect.android.preferences.PreferencesDataSource;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class QuestionTextSizeHelper {

    private final PreferencesDataSource generalPrefs;

    public QuestionTextSizeHelper(PreferencesDataSource generalPrefs) {
        this.generalPrefs = generalPrefs;
    }

    public float getHeadline6() {
        return getBaseFontSize() - 1; // 20sp by default
    }

    public float getSubtitle1() {
        return getBaseFontSize() - 5; // 16sp by default
    }

    private int getBaseFontSize() {
        return Integer.parseInt(String.valueOf(generalPrefs.getString(KEY_FONT_SIZE)));
    }
}
