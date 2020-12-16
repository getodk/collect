package org.odk.collect.android.preferences;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_COMPLETED_DEFAULT;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesTest {

    @Test
    public void generalSharedPreferences_loadDefaultPreferences_loadsDefaults() {
        new GeneralSharedPreferences(RuntimeEnvironment.application).loadDefaultPreferences();
        HashMap<String, Object> defaultValues = GeneralKeys.DEFAULTS;

        GeneralSharedPreferences generalSharedPreferences = GeneralSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            assertThat(generalSharedPreferences.get(key), equalTo(defaultValues.get(key)));
        }
    }

    @Test
    public void adminSharedPreferences_loadDefaultPreferences_loadsDefaults() {
        AdminSharedPreferences adminSharedPreferences = new AdminSharedPreferences(RuntimeEnvironment.application);
        adminSharedPreferences.loadDefaultPreferences();

        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            assertThat(adminSharedPreferences.get(key), equalTo(adminSharedPreferences.getDefault(key)));
        }
    }

    @Test
    public void generalSharedPreferencesUpgradeTest() {
        GeneralSharedPreferences generalSharedPreferences = new GeneralSharedPreferences(RuntimeEnvironment.application);
        generalSharedPreferences.save(KEY_COMPLETED_DEFAULT, false);
        generalSharedPreferences.reloadPreferences();

        HashMap<String, Object> defaultValues = GeneralKeys.DEFAULTS;
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
        AdminSharedPreferences adminSharedPreferences = new AdminSharedPreferences(RuntimeEnvironment.application);
        adminSharedPreferences.save(KEY_EDIT_SAVED, false);
        adminSharedPreferences.reloadPreferences();

        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            if (key.equals(KEY_EDIT_SAVED)) {
                assertThat(adminSharedPreferences.get(key), equalTo(false));
            } else {
                assertThat(adminSharedPreferences.get(key), equalTo(adminSharedPreferences.getDefault(key)));
            }
        }
    }
}
