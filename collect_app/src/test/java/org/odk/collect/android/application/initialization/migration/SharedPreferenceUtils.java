package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;

import static org.junit.Assert.assertEquals;

public class SharedPreferenceUtils {

    private SharedPreferenceUtils() {
    }

    public static void initPrefs(Settings prefs, Object... pairs) {
        prefs.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            prefs.save((String) pairs[i], pairs[i + 1]);
        }
    }

    public static Settings initPrefs(Object... pairs) {
        Settings prefs = TestSettingsProvider.getTestSettings("test");
        initPrefs(prefs, pairs);
        return prefs;
    }

    public static void assertPrefs(Settings prefs, Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            assertEquals(pairs[i + 1], prefs.getAll().get(pairs[i]));
        }
        assertEquals(pairs.length / 2, prefs.getAll().size());
    }

    public static void assertPrefsEmpty(Settings prefs) {
        assertEquals(0, prefs.getAll().size());
    }
}
