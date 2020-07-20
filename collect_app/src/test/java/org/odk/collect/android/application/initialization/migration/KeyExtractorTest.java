package org.odk.collect.android.application.initialization.migration;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.extractNewKey;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
public class KeyExtractorTest {

    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        prefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);
    }

    @Test
    public void whenNewKeyExists_doesNothing() {
        initPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValues("blah1", "blah2")
                .apply(prefs);

        assertPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );
    }

    @Test
    public void whenOldKeyMissing_doesNothing() {
        initPrefs(prefs);

        extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValues("blah1", "blah2")
                .apply(prefs);

        assertPrefsEmpty(prefs);
    }
}