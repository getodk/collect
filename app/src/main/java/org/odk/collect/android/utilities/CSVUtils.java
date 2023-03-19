package org.odk.collect.android.utilities;

import org.jetbrains.annotations.Contract;

/**
 * Escape strings according to https://tools.ietf.org/html/rfc4180
 */
public final class CSVUtils {

    private CSVUtils() {

    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static String getEscapedValueForCsv(String value) {
        if (value == null) {
            return null;
        }

        return quoteStringIfNeeded(escapeDoubleQuote(value));
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    private static String escapeDoubleQuote(String value) {
        if (value == null) {
            return null;
        }

        return value.replaceAll("\"", "\"\"");
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    private static String quoteStringIfNeeded(String value) {
        if (value == null) {
            return null;
        }

        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }
}