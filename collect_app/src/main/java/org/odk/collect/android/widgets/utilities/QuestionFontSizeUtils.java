package org.odk.collect.android.widgets.utilities;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_FONT_SIZE;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.shared.settings.Settings;

public final class QuestionFontSizeUtils {

    public enum FontSize {
        HEADLINE_6,
        SUBTITLE_1,
        BODY_MEDIUM,
        BODY_LARGE
    }

    public static final int DEFAULT_FONT_SIZE = 21;

    private static final int HEADLINE_6_DIFF = -1;

    private static final int SUBTITLE_1_DIFF = -5;

    private static final int BODY_MEDIUM_DIFF = -7;

    private static final int BODY_LARGE_DIFF = -5;

    private QuestionFontSizeUtils() {

    }

    public static int getFontSize(Settings settings, FontSize fontSize) {
        int settingsValue = Integer.parseInt(settings.getString(KEY_FONT_SIZE));

        switch (fontSize) {
            case HEADLINE_6 -> {
                return settingsValue + HEADLINE_6_DIFF;
            }

            case SUBTITLE_1 -> {
                return settingsValue + SUBTITLE_1_DIFF;
            }

            case BODY_MEDIUM -> {
                return settingsValue + BODY_MEDIUM_DIFF;
            }

            case BODY_LARGE -> {
                return settingsValue + BODY_LARGE_DIFF;
            }

            default -> throw new IllegalArgumentException();
        }
    }

    /**
     * @deprecated Use {@link QuestionFontSizeUtils#getFontSize(Settings, FontSize)} instead
     *
     */
    @Deprecated
    public static int getQuestionFontSize() {
        try {
            int fontSize = Integer.parseInt(DaggerUtils.getComponent(Collect.getInstance()).settingsProvider().getUnprotectedSettings().getString(KEY_FONT_SIZE));
            return fontSize + HEADLINE_6_DIFF;
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
