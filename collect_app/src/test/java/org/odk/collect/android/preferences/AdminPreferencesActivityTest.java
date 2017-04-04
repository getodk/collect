package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.FragmentTestUtil.startFragment;

/** Tests for Admin Preferences */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AdminPreferencesActivityTest {

    private AdminPreferencesFragment adminPreferencesFragment;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        adminPreferencesFragment = new AdminPreferencesFragment();
        startFragment(adminPreferencesFragment);

        sharedPreferences = adminPreferencesFragment.getActivity().
                getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
    }

    @Test
    public void shouldUpdateAdminSharedPreferences() throws NullPointerException {
        for (String adminKey : AdminKeys.ALL_KEYS) {
            Preference preference = adminPreferencesFragment.findPreference(adminKey);
            if (preference instanceof CheckBoxPreference) {
                System.out.println("Testing " + adminKey);
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;

                assertNotNull("Preference not found: " + adminKey, checkBoxPreference);
                checkBoxPreference.setChecked(true);
                boolean actual = sharedPreferences.getBoolean(adminKey, false);
                assertTrue("Error in preference " + adminKey, actual);

                checkBoxPreference.setChecked(false);
                actual = sharedPreferences.getBoolean(adminKey, true);
                assertFalse("Error in preference " + adminKey, actual);
            }
        }
    }
}
