package org.odk.collect.android.preferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_COMPLETED_DEFAULT;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesTest {

    @Before
    public void setup() {
        // Needed otherwise we run into cached data (in the static instance) from other tests
        GeneralSharedPreferences.getInstance().clear();
        AdminSharedPreferences.getInstance().clear();
    }

    @Test
    public void generalSharedPreferences_loadDefaultPreferences_loadsDefaults() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        HashMap<String, Object> defaultValues = GeneralKeys.DEFAULTS;

        GeneralSharedPreferences generalSharedPreferences = GeneralSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            assertThat(generalSharedPreferences.get(key), equalTo(defaultValues.get(key)));
        }
    }

    @Test
    public void adminSharedPreferences_loadDefaultPreferences_loadsDefaults() {
        AdminSharedPreferences.getInstance().loadDefaultPreferences();

        AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            assertThat(adminSharedPreferences.get(key), equalTo(adminSharedPreferences.getDefault(key)));
        }
    }

    @Test
    public void generalSharedPreferencesUpgradeTest() {
        GeneralSharedPreferences.getInstance().save(KEY_COMPLETED_DEFAULT, false);
        GeneralSharedPreferences.getInstance().reloadPreferences();

        HashMap<String, Object> defaultValues = GeneralKeys.DEFAULTS;
        GeneralSharedPreferences generalSharedPreferences = GeneralSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            if (key.equals(KEY_COMPLETED_DEFAULT)) {
                assertThat(generalSharedPreferences.get(key), equalTo(false));
            } else {
                assertThat(generalSharedPreferences.get(key), equalTo(defaultValues.get(key)));
            }
        }
    }

    @Test
    public void adminSharedPreferencesUpgradeTest() {
        AdminSharedPreferences.getInstance().save(KEY_EDIT_SAVED, false);
        AdminSharedPreferences.getInstance().reloadPreferences();

        AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            if (key.equals(KEY_EDIT_SAVED)) {
                assertThat(adminSharedPreferences.get(key), equalTo(false));
            } else {
                assertThat(adminSharedPreferences.get(key), equalTo(adminSharedPreferences.getDefault(key)));
            }
        }
    }
}
