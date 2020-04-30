package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import static org.junit.Assert.assertEquals;

public class SharedPreferenceUtils {

    private SharedPreferenceUtils() {

    }

    public static void initPrefs(SharedPreferences prefs, Object... pairs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (pairs[i + 1] instanceof String) {
                editor.putString((String) pairs[i], (String) pairs[i + 1]);
            } else if (pairs[i + 1] instanceof Boolean) {
                editor.putBoolean((String) pairs[i], (Boolean) pairs[i + 1]);
            } else if (pairs[i + 1] instanceof Long) {
                editor.putLong((String) pairs[i], (Long) pairs[i + 1]);
            } else {
                throw new IllegalArgumentException(pairs[i + 1].getClass().getSimpleName() + " not supported!");
            }
        }
        editor.commit();
    }

    public static void assertPrefs(SharedPreferences prefs, Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            assertEquals(pairs[i + 1], prefs.getAll().get(pairs[i]));
        }
        assertEquals(pairs.length / 2, prefs.getAll().size());
    }

    public static void assertPrefsEmpty(SharedPreferences prefs) {
        assertEquals(0, prefs.getAll().size());
    }
}
