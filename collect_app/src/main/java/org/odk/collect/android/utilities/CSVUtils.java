package org.odk.collect.android.utilities;

import org.jetbrains.annotations.Contract;

public class CSVUtils {

    private CSVUtils() {

    }

    /**
     * Escapes quotes and then wraps in quotes for output to CSV.
     */
    @Contract("null -> null")
    public static String getEscapedValueForCsv(String value) {
        if (value == null) {
            return null;
        }

        if (value.contains("\"")) {
            value = escapeDoubleQuote(value);
        }

        return quoteString(value);
    }

    @Contract("null -> null; !null -> !null")
    public static String escapeDoubleQuote(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\"", "\"\"");
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static String quoteString(String value) {
        if (value == null) {
            return null;
        }
        return "\"" + value + "\"";
    }
}
