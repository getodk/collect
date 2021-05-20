package org.odk.collect.android.preferences.screens;

import android.content.Intent;

import androidx.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectHelpers;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;

@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(ParameterizedRobolectricTestRunner.class)
public class GeneralPreferencesActivityTest {

    private GeneralPreferencesFragment generalPreferencesFragment;
    private ActivityController<GeneralPreferencesActivity> activityController;
    private final boolean accessedFromAdminSettings;

    public GeneralPreferencesActivityTest(boolean accessedFromAdminSettings) {
        this.accessedFromAdminSettings = accessedFromAdminSettings;
    }

    @ParameterizedRobolectricTestRunner.Parameters()
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true}, {false}
        });
    }

    @Before
    public void setUp() throws Exception {
        CollectHelpers.setupDemoProject();

        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_ADMIN_MODE, accessedFromAdminSettings);
        activityController = Robolectric
                .buildActivity(GeneralPreferencesActivity.class, intent)
                .setup();

        generalPreferencesFragment = (GeneralPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);
    }

    @Test
    public void whenGeneralPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        assertThat(generalPreferencesFragment.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenServerPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("protocol");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        ServerPreferencesFragment preferences
                = (ServerPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenUserInterfacePreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("user_interface");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        UserInterfacePreferencesFragment preferences
                = (UserInterfacePreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenMapsPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("maps");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        MapsPreferencesFragment preferences
                = (MapsPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenFormManagementPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("form_management");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        FormManagementPreferencesFragment preferences
                = (FormManagementPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenUserAndDeviceIdentityPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("user_and_device_identity");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        IdentityPreferencesFragment preferences
                = (IdentityPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

    @Test
    public void whenExperimentalPreferencesDisplayed_shouldIsInAdminModeValueBePassedToFragment() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("experimental");

        generalPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        ExperimentalPreferencesFragment preferences
                = (ExperimentalPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(accessedFromAdminSettings));
    }

}
