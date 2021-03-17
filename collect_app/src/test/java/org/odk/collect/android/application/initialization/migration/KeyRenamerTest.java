package org.odk.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.source.Settings;
import org.robolectric.RobolectricTestRunner;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.renameKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyRenamerTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void renamesKeys() {
        initPrefs(prefs,
                "colour", "red"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertPrefs(prefs,
                "couleur", "red"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        initPrefs(prefs,
                "colour", "red",
                "couleur", "blue"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertPrefs(prefs,
                "colour", "red",
                "couleur", "blue"
        );
    }
}