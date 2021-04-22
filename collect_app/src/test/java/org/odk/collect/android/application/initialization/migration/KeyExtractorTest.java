package org.odk.collect.android.application.initialization.migration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.extractNewKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
public class KeyExtractorTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void createsNewKeyBasedOnExistingKeysValue() {
        initPrefs(prefs,
                "oldKey", "blah"
        );

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("blah").toValue("newBlah")
                .apply(prefs);

        assertPrefs(prefs,
                "oldKey", "blah",
                "newKey", "newBlah"
        );
    }

    @Test
    public void whenNewKeyExists_doesNothing() {
        initPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        assertPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );
    }

    @Test
    public void whenOldKeyMissing_doesNothing() {
        initPrefs(prefs);

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        assertPrefsEmpty(prefs);
    }
}
