package com.amazonaws.mobile.util;

public final class StringFormatUtils {

    /** This utility class is not constructable. */
    private StringFormatUtils() {
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @param higherPrecision flag to show two more digits of precision after the decimal.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(final long bytes, final boolean higherPrecision) {
        final String[] quantifiers = new String[] {
            "KB", "MB", "GB", "TB"
        };
        double size = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "\u221E";
            }
            size /= 1024;
            if (size < 512) {
                if (higherPrecision) {
                    return String.format("%.2f %s", size, quantifiers[i]);
                } else {
                    return String.format("%d %s", Math.round(size), quantifiers[i]);
                }
            }
        }
    }
}
