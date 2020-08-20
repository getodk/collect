package org.odk.collect.android.preferences;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

import timber.log.Timber;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

/**
 * Tests for Admin Preferences
 */
@LooperMode(PAUSED)
@RunWith(RobolectricTestRunner.class)
public class AdminPreferencesActivityTest {

    private AdminPreferencesFragment adminPreferencesFragment;
    private ActivityController<AdminPreferencesActivity> activityController;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        activityController = Robolectric
                .buildActivity(AdminPreferencesActivity.class)
                .setup();

        adminPreferencesFragment = (AdminPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        sharedPreferences = activityController
                .get()
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

    @Test
    public void whenAdminPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        assertThat(adminPreferencesFragment.isInAdminMode(), is(true));
    }

    @Test
    public void whenGeneralPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("odk_preferences");

        adminPreferencesFragment.onPreferenceClick(preference);

        Intent expectedIntent = new Intent(activityController.get(), PreferencesActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
        assertThat(actual.getExtras().getBoolean(INTENT_KEY_ADMIN_MODE), is(true));
    }

    @Test
    public void whenMainMenuPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("main_menu");

        adminPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        AdminPreferencesFragment.MainMenuAccessPreferences preferences
                = (AdminPreferencesFragment.MainMenuAccessPreferences) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(true));
    }

    @Test
    public void whenUserPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("user_settings");

        adminPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        AdminPreferencesFragment.UserSettingsAccessPreferences preferences
                = (AdminPreferencesFragment.UserSettingsAccessPreferences) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(true));
    }

    @Test
    public void whenFormEntryPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("form_entry");

        adminPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        AdminPreferencesFragment.FormEntryAccessPreferences preferences
                = (AdminPreferencesFragment.FormEntryAccessPreferences) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(true));
    }
}
