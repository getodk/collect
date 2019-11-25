package org.odk.collect.android.utilities;

import static java.lang.Character.isWhitespace;

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String string) {
        char[] chars = string.toCharArray();

        for (char character : chars) {
            if (!isWhitespace(character)) {
                return false;
            }
        }

        return true;
    }
}
