package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.extractNewKey;
import static org.odk.collect.settings.support.SettingsUtils.assertSettings;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

public class KeyExtractorTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void createsNewKeyBasedOnExistingKeysValue() {
        initSettings(prefs,
                "oldKey", "blah"
        );

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("blah").toValue("newBlah")
                .apply(prefs);

        assertSettings(prefs,
                "oldKey", "blah",
                "newKey", "newBlah"
        );
    }

    @Test
    public void whenNewKeyExists_doesNothing() {
        initSettings(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        assertSettings(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );
    }

    @Test
    public void whenOldKeyMissing_doesNothing() {
        initSettings(prefs);

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        assertSettings(prefs);
    }
}
