package org.odk.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.utilities.TestPreferencesProvider;
import org.robolectric.RobolectricTestRunner;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.removeKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyRemoverTest {

    private final PreferencesDataSource prefs = TestPreferencesProvider.getTestPreferences("test");

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initPrefs(prefs);

        removeKey("blah").apply(prefs);

        assertPrefsEmpty(prefs);
    }
}
