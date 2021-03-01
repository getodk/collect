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

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.renameKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyRenamerTest {

    private PreferencesDataSource prefs;

    @Before
    public void setUp() throws Exception {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        prefs = component.preferencesRepository().getTestPreferences("test");
    }

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