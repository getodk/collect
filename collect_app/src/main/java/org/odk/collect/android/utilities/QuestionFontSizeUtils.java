package org.odk.collect.android.utilities;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_FONT_SIZE;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;

public final class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(DaggerUtils.getComponent(Collect.getInstance()).settingsProvider().getUnprotectedSettings().getString(KEY_FONT_SIZE));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
