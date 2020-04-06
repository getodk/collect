package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class FontSizeProvider {
    public static final int DEFAULT_FONT_SIZE = 21;

    public static int getQuestionFontSize() {
        // For testing:
        Collect instance = Collect.getInstance();
        if (instance == null) {
            return DEFAULT_FONT_SIZE;
        }

        return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
    }
}
