package org.odk.collect.android.utilities;

import androidx.test.core.app.ApplicationProvider;

import org.odk.collect.android.preferences.PreferencesRepository;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(String.valueOf(new PreferencesRepository(ApplicationProvider.getApplicationContext()).getGeneralPreferences().getString(KEY_FONT_SIZE)));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
