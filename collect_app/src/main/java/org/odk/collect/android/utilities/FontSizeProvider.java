package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class FontSizeProvider {
    public static final String DEFAULT_FONTSIZE = "21";
    public static final int DEFAULT_FONTSIZE_INT = 21;

    public static int getQuestionFontsize() {
        // For testing:
        Collect instance = Collect.getInstance();
        if (instance == null) {
            return DEFAULT_FONTSIZE_INT;
        }

        return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
    }
}
