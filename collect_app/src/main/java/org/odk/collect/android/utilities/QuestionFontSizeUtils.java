package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;

public class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(DaggerUtils.getComponent(Collect.getInstance()).settingsProvider().getGeneralSettings().getString(KEY_FONT_SIZE));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
