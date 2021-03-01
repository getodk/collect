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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.moveKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyMoverTest {

    private PreferencesDataSource prefs;
    private PreferencesDataSource other;

    @Before
    public void setUp() throws Exception {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        prefs = component.preferencesRepository().getTestPreferences("test");
        other = component.preferencesRepository().getTestPreferences("other");
    }

    @Test
    public void movesKeyAndValueToOtherPrefs() {
        initPrefs(prefs,
                "key", "value"
        );

        moveKey("key")
                .toPreferences(other)
                .apply(prefs);

        assertThat(prefs.getAll().size(), is(0));
        assertPrefs(other,
                "key", "value"
        );
    }

    @Test
    public void whenKeyNotInOriginalPrefs_doesNothing() {
        moveKey("key")
                .toPreferences(other)
                .apply(prefs);

        assertThat(prefs.getAll().size(), is(0));
        assertThat(other.getAll().size(), is(0));
    }

    @Test
    public void whenKeyInOtherPrefs_doesNothing() {
        initPrefs(prefs,
                "key", "value"
        );

        initPrefs(other,
                "key", "other-value"
        );

        moveKey("key")
                .toPreferences(other)
                .apply(prefs);

        assertPrefs(prefs,
                "key", "value"
        );

        assertPrefs(other,
                "key", "other-value"
        );
    }
}