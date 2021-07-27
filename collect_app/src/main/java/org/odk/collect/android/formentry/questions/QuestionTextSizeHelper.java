package org.odk.collect.android.formentry.questions;

import org.odk.collect.shared.Settings;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;

public class QuestionTextSizeHelper {

    private final Settings generalSettings;

    public QuestionTextSizeHelper(Settings generalSettings) {
        this.generalSettings = generalSettings;
    }

    public float getHeadline6() {
        return getBaseFontSize() - 1; // 20sp by default
    }

    public float getSubtitle1() {
        return getBaseFontSize() - 5; // 16sp by default
    }

    private int getBaseFontSize() {
        return Integer.parseInt(String.valueOf(generalSettings.getString(KEY_FONT_SIZE)));
    }
}
