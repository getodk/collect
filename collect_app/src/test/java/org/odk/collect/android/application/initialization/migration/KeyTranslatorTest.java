package org.odk.collect.android.application.initialization.migration;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.robolectric.RobolectricTestRunner;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyTranslatorTest {

    private PreferencesDataSource prefs;

    @Before
    public void setUp() throws Exception {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        prefs = component.preferencesRepository().getTestPreferences("test");
    }

    @Test
    public void renamesKeyAndTranslatesValues() {
        initPrefs(prefs,
                "colour", "red"
        );

        translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertPrefs(prefs,
                "couleur", "rouge"
        );
    }

    @Test
    public void canTranslateMultipleValues() {
        KeyTranslator translator = translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .fromValue("green")
                .toValue("vert");

        initPrefs(prefs,
                "colour", "red"
        );

        translator.apply(prefs);

        assertPrefs(prefs,
                "couleur", "rouge"
        );

        initPrefs(prefs,
                "colour", "green"
        );

        translator.apply(prefs);

        assertPrefs(prefs,
                "couleur", "vert"
        );
    }

    @Test
    public void whenKeyHasUnknownValue_doesNotDoAnything() {
        initPrefs(prefs,
                "colour", "blue"
        );

        translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertPrefs(prefs,
                "colour", "blue"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        initPrefs(prefs,
                "colour", "red",
                "couleur", "bleu"
        );

        translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertPrefs(prefs,
                "colour", "red",
                "couleur", "bleu"
        );
    }
}
