package org.odk.collect.android.application.initialization.migration;

import android.content.Context;
import android.content.SharedPreferences;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.odk.collect.android.utilities.SharedPreferencesUtils.put;

public class SharedPreferenceUtils {

    private SharedPreferenceUtils() {

    }

    public static void initPrefs(SharedPreferences prefs, Object... pairs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            put(editor, (String) pairs[i], pairs[i + 1]);
        }
        editor.commit();
    }

    public static SharedPreferences initPrefs(Object... pairs) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);
        initPrefs(prefs, pairs);
        return prefs;
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
