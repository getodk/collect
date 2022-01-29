package org.odk.collect.android.configure.migration;

import static org.odk.collect.android.configure.migration.MigrationUtils.removeKey;
import static org.odk.collect.android.configure.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.configure.migration.SharedPreferenceUtils.initPrefs;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;

@RunWith(AndroidJUnit4.class)
public class KeyRemoverTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initPrefs(prefs);

        removeKey("blah").apply(prefs);

        assertPrefsEmpty(prefs);
    }
}
