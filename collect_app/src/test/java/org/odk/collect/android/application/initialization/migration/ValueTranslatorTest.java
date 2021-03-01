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

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateValue;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class ValueTranslatorTest {

    private PreferencesDataSource prefs;

    @Before
    public void setUp() throws Exception {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        prefs = component.preferencesRepository().getTestPreferences("test");
    }

    @Test
    public void translatesValueForKey() {
        initPrefs(prefs,
                "key", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "newValue"
        );
    }

    @Test
    public void doesNotTranslateOtherValues() {
        initPrefs(prefs,
                "key", "otherValue"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "otherValue"
        );
    }

    @Test
    public void whenKeyNotInPrefs_doesNothing() {
        initPrefs(prefs,
                "otherKey", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "otherKey", "value"
        );
    }
}