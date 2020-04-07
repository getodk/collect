package org.odk.collect.android.utilities;

import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class QuestionFontSizeProvider {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeProvider() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
