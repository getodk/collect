package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.renameKey;
import static org.odk.collect.settings.support.SettingsUtils.assertSettings;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

public class KeyRenamerTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void renamesKeys() {
        initSettings(prefs,
                "colour", "red"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertSettings(prefs,
                "couleur", "red"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        initSettings(prefs,
                "colour", "red",
                "couleur", "blue"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertSettings(prefs,
                "colour", "red",
                "couleur", "blue"
        );
    }
}
