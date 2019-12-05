package org.odk.collect.android.utilities;

public class CSVUtils {

    private CSVUtils() {

    }

    /**
     * Escapes quotes and then wraps in quotes for output to CSV.
     */
    public static String getEscapedValueForCsv(String value) {
        if (value.contains("\"")) {
            value = value.replaceAll("\"", "\"\"");
        }

        return "\"" + value + "\"";
    }
}
