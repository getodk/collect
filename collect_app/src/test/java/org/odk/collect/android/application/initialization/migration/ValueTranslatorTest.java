package org.odk.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.utilities.TestPreferencesProvider;
import org.robolectric.RobolectricTestRunner;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateValue;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class ValueTranslatorTest {

    private final PreferencesDataSource prefs = TestPreferencesProvider.getTestPreferences("test");

    @Test
    public void translatesValueForKey() {
        initPrefs(prefs,
                "key", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "newValue"
        );
    }

    @Test
    public void doesNotTranslateOtherValues() {
        initPrefs(prefs,
                "key", "otherValue"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "otherValue"
        );
    }

    @Test
    public void whenKeyNotInPrefs_doesNothing() {
        initPrefs(prefs,
                "otherKey", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "otherKey", "value"
        );
    }
}