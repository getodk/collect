package org.odk.collect.android.application.initialization.migration;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.translateValue;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class ValueTranslatorTest {

    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        prefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);
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