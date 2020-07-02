package odk.hedera.collect.utilities;

import odk.hedera.collect.preferences.GeneralSharedPreferences;

import static odk.hedera.collect.preferences.GeneralKeys.KEY_FONT_SIZE;

public class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
