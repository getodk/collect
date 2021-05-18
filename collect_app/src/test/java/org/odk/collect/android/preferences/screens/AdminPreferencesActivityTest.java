package org.odk.collect.android.preferences.screens;

import android.content.Intent;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.projects.InMemProjectsRepository;
import org.odk.collect.projects.Project;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.UUIDGenerator;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import timber.log.Timber;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests for Admin Preferences
 */
@RunWith(AndroidJUnit4.class)
public class AdminPreferencesActivityTest {

    private AdminPreferencesFragment adminPreferencesFragment;
    private ActivityController<AdminPreferencesActivity> activityController;
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

    @Before
    public void setUp() throws Exception {
        InMemProjectsRepository projectsRepository = new InMemProjectsRepository(new UUIDGenerator());
        Project project = new Project("name", "icon", "#ffffff", "id");
        projectsRepository.save(project);
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ProjectsRepository providesProjectsRepository(UUIDGenerator uuidGenerator, Gson gson, SettingsProvider settingsProvider) {
                return projectsRepository;
            }

            @Override
            public CurrentProjectProvider providesCurrentProjectProvider(SettingsProvider settingsProvider, ProjectsRepository projectsRepository) {
                CurrentProjectProvider currentProjectProvider = super.providesCurrentProjectProvider(settingsProvider, projectsRepository);
                currentProjectProvider.setCurrentProject(project.getUuid());
                return currentProjectProvider;
            }
        });

        activityController = Robolectric
                .buildActivity(AdminPreferencesActivity.class)
                .setup();

        adminPreferencesFragment = (AdminPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);
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
                boolean actual = adminSettings.getBoolean(adminKey);
                assertTrue("Error in preference " + adminKey, actual);

                checkBoxPreference.setChecked(false);
                actual = adminSettings.getBoolean(adminKey);
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

        Intent expectedIntent = new Intent(activityController.get(), GeneralPreferencesActivity.class);
        Intent actual = shadowOf((Collect) ApplicationProvider.getApplicationContext()).getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
        assertThat(actual.getExtras().getBoolean(INTENT_KEY_ADMIN_MODE), is(true));
    }

    @Test
    public void whenMainMenuPreferencesDisplayed_shouldIsInAdminModeReturnTrue() {
        Preference preference = mock(Preference.class);
        when(preference.getKey()).thenReturn("main_menu");

        adminPreferencesFragment.onPreferenceClick(preference);
        activityController.resume();

        MainMenuAccessPreferencesFragment preferences
                = (MainMenuAccessPreferencesFragment) activityController.get()
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

        UserSettingsAccessPreferencesFragment preferences
                = (UserSettingsAccessPreferencesFragment) activityController.get()
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

        FormEntryAccessPreferencesFragment preferences
                = (FormEntryAccessPreferencesFragment) activityController.get()
                .getSupportFragmentManager()
                .findFragmentById(R.id.preferences_fragment_container);

        assertThat(preferences.isInAdminMode(), is(true));
    }
}
