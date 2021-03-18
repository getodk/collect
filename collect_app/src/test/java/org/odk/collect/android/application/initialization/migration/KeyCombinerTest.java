package org.odk.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.source.Settings;
import org.robolectric.RobolectricTestRunner;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.combineKeys;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyCombinerTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void combinesValuesOfTwoKeys_andRemovesOldKeys() {
        initPrefs(prefs,
                "red", true,
                "blue", true
        );

        combineKeys("red", "blue")
                .withValues(true, true)
                .toPairs("color", "purple")
                .apply(prefs);

        assertPrefs(prefs,
                "color", "purple"
        );
    }

    @Test
    public void canCombineMultipleValues() {
        KeyCombiner combiner = combineKeys("red", "blue")
                .withValues(true, true).toPairs("color", "purple")
                .withValues(false, true).toPairs("color", "blue")
                .withValues(true, false).toPairs("color", "red");

        initPrefs(prefs,
                "red", true,
                "blue", false
        );

        combiner.apply(prefs);

        assertPrefs(prefs,
                "color", "red"
        );

        initPrefs(prefs,
                "red", false,
                "blue", true
        );

        combiner.apply(prefs);

        assertPrefs(prefs,
                "color", "blue"
        );
    }

    @Test
    public void whenCombinedKeyExists_removesOtherKey_andModifiesExistingKey() {
        initPrefs(prefs,
                "direction", "north",
                "other-direction", "west"
        );

        combineKeys("direction", "other-direction")
                .withValues("north", "west")
                .toPairs("direction", "north-west")
                .apply(prefs);

        assertPrefs(prefs,
                "direction", "north-west"
        );
    }
}