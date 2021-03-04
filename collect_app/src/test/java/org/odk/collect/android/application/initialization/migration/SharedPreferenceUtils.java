package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.junit.Assert.assertEquals;

public class SharedPreferenceUtils {

    private SharedPreferenceUtils() {
    }

    public static void initPrefs(PreferencesDataSource prefs, Object... pairs) {
        prefs.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            prefs.save((String) pairs[i], pairs[i + 1]);
        }
    }

    public static PreferencesDataSource initPrefs(Object... pairs) {
        PreferencesDataSource prefs = TestPreferencesProvider.getTestPreferences("test");
        initPrefs(prefs, pairs);
        return prefs;
    }

    public static void assertPrefs(PreferencesDataSource prefs, Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            assertEquals(pairs[i + 1], prefs.getAll().get(pairs[i]));
        }
        assertEquals(pairs.length / 2, prefs.getAll().size());
    }

    public static void assertPrefsEmpty(PreferencesDataSource prefs) {
        assertEquals(0, prefs.getAll().size());
    }
}
