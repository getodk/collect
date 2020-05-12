package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

public class MigrationUtils {

    private MigrationUtils() {

    }

    public static ValueTranslator translateValue(String value) {
        return new ValueTranslator(value);
    }

    static void put(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
    }
}