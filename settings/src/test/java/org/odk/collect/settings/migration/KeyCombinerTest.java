package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.combineKeys;
import static org.odk.collect.settings.support.SettingsUtils.assertSettings;
import static org.odk.collect.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import org.odk.collect.shared.settings.InMemSettings;
import org.odk.collect.shared.settings.Settings;

public class KeyCombinerTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void combinesValuesOfTwoKeys_andRemovesOldKeys() {
        initSettings(prefs,
                "red", true,
                "blue", true
        );

        combineKeys("red", "blue")
                .withValues(true, true)
                .toPairs("color", "purple")
                .apply(prefs);

        assertSettings(prefs,
                "color", "purple"
        );
    }

    @Test
    public void canCombineMultipleValues() {
        KeyCombiner combiner = combineKeys("red", "blue")
                .withValues(true, true).toPairs("color", "purple")
                .withValues(false, true).toPairs("color", "blue")
                .withValues(true, false).toPairs("color", "red");

        initSettings(prefs,
                "red", true,
                "blue", false
        );

        combiner.apply(prefs);

        assertSettings(prefs,
                "color", "red"
        );

        initSettings(prefs,
                "red", false,
                "blue", true
        );

        combiner.apply(prefs);

        assertSettings(prefs,
                "color", "blue"
        );
    }

    @Test
    public void whenCombinedKeyExists_removesOtherKey_andModifiesExistingKey() {
        initSettings(prefs,
                "direction", "north",
                "other-direction", "west"
        );

        combineKeys("direction", "other-direction")
                .withValues("north", "west")
                .toPairs("direction", "north-west")
                .apply(prefs);

        assertSettings(prefs,
                "direction", "north-west"
        );
    }
}
