package org.odk.collect.android.formentry;

import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

class QuestionTextSizeHelper {

    public float getSubtitle1() {
        return getBaseFontSize() - 5; // 16dp by default
    }

    private int getBaseFontSize() {
        return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
    }
}
