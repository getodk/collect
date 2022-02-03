package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.removeKey;
import static org.odk.collect.settings.support.SettingsUtils.assertSettingsEmpty;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

public class KeyRemoverTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initSettings(prefs);

        removeKey("blah").apply(prefs);

        assertSettingsEmpty(prefs);
    }
}
