package org.odk.collect.android.formentry.questions;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_FONT_SIZE;

import org.odk.collect.shared.settings.Settings;

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
