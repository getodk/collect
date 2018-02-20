package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import timber.log.Timber;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Admin Preferences
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AdminPreferencesActivityTest {

    private AdminPreferencesFragment adminPreferencesFragment;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        AdminPreferencesActivity activity = Robolectric.setupActivity(AdminPreferencesActivity.class);

        adminPreferencesFragment = (AdminPreferencesFragment) activity
                .getFragmentManager().findFragmentByTag(AdminPreferencesActivity.TAG);

        sharedPreferences = adminPreferencesFragment.getActivity()
                .getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
    }

    @Test
    public void shouldUpdateAdminSharedPreferences() throws NullPointerException {
        for (String adminKey : AdminKeys.ALL_KEYS) {
            Preference preference = adminPreferencesFragment.findPreference(adminKey);
            if (preference instanceof CheckBoxPreference) {
                Timber.d("Testing %s", adminKey);
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
