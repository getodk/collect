package odk.hedera.collect.application.initialization.migration;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static odk.hedera.collect.application.initialization.migration.MigrationUtils.removeKey;
import static odk.hedera.collect.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static odk.hedera.collect.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyRemoverTest {

    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        prefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);
    }

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initPrefs(prefs);

        removeKey("blah").apply(prefs);

        assertPrefsEmpty(prefs);
    }
}
