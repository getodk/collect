package org.odk.collect.android.preferences.screens

import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.strings.UUIDGenerator
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ProjectDisplayPreferencesFragmentTest {
    lateinit var currentProjectProvider: CurrentProjectProvider
    lateinit var projectsRepository: ProjectsRepository

    @Before
    fun setup() {
        currentProjectProvider = mock(CurrentProjectProvider::class.java)
        projectsRepository = mock(ProjectsRepository::class.java)

        `when`(currentProjectProvider.getCurrentProject())
            .thenReturn(Project.Saved("123", "Project X", "X", "#cccccc"))

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectProvider(settingsProvider: SettingsProvider, projectsRepository: ProjectsRepository): CurrentProjectProvider {
                return currentProjectProvider
            }

            override fun providesProjectsRepository(uuidGenerator: UUIDGenerator, gson: Gson, settingsProvider: SettingsProvider): ProjectsRepository {
                return projectsRepository
            }
        })
    }

    @Test
    fun `Project Name preference should be visible`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_NAME_KEY)!!.isVisible,
                `is`(true)
            )
        }
    }

    @Test
    fun `Project Name preference should have proper title`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_NAME_KEY)!!.title,
                `is`(
                    TranslationHandler.getString(
                        ApplicationProvider.getApplicationContext<Collect>(),
                        R.string.project_name
                    )
                )
            )
        }
    }

    @Test
    fun `Project Name preference should have proper summary`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_NAME_KEY)!!.summary,
                `is`("Project X")
            )
        }
    }

    @Test
    fun `Project Icon preference should be visible`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_ICON_KEY)!!.isVisible,
                `is`(true)
            )
        }
    }

    @Test
    fun `Project Icon preference should have proper title`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_ICON_KEY)!!.title,
                `is`(
                    TranslationHandler.getString(
                        ApplicationProvider.getApplicationContext<Collect>(),
                        R.string.project_icon
                    )
                )
            )
        }
    }

    @Test
    fun `Project Icon preference should have proper summary`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<EditTextPreference>(ProjectDisplayPreferencesFragment.PROJECT_ICON_KEY)!!.summary,
                `is`("X")
            )
        }
    }

    @Test
    fun `Project Color preference should be visible`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<Preference>(ProjectDisplayPreferencesFragment.PROJECT_COLOR_KEY)!!.isVisible,
                `is`(true)
            )
        }
    }

    @Test
    fun `Project Color preference should have proper title`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<Preference>(ProjectDisplayPreferencesFragment.PROJECT_COLOR_KEY)!!.title,
                `is`(
                    TranslationHandler.getString(
                        ApplicationProvider.getApplicationContext<Collect>(),
                        R.string.project_color
                    )
                )
            )
        }
    }

    @Test
    fun `Project Color preference should have proper summary`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectDisplayPreferencesFragment::class.java, R.style.ThemeOverlay_MaterialComponents)
        scenario.onFragment {
            assertThat(
                it.findPreference<Preference>(ProjectDisplayPreferencesFragment.PROJECT_COLOR_KEY)!!.summary.toString(),
                `is`("■")
            )
        }
    }
}
