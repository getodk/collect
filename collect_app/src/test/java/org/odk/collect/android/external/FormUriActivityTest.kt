package org.odk.collect.android.external

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.strings.UUIDGenerator
import org.odk.collect.testshared.RecordedIntentsRule

@RunWith(AndroidJUnit4::class)
class FormUriActivityTest {
    private lateinit var projectsRepository: ProjectsRepository
    private val currentProjectProvider = mock<CurrentProjectProvider>()
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val firstProject = Project.DEMO_PROJECT
    private val secondProject = Project.Saved("123", "Second project", "S", "#cccccc")

    @get:Rule
    val activityRule = RecordedIntentsRule()

    @Before
    fun setup() {
        projectsRepository = InMemProjectsRepository()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectsRepository(
                uuidGenerator: UUIDGenerator?,
                gson: Gson?,
                settingsProvider: SettingsProvider?
            ): ProjectsRepository {
                return projectsRepository
            }

            override fun providesCurrentProjectProvider(
                settingsProvider: SettingsProvider?,
                projectsRepository: ProjectsRepository?
            ): CurrentProjectProvider {
                return currentProjectProvider
            }
        })
    }

    @Test
    fun `When there are no projects then display alert dialog`() {
        val scenario = ActivityScenario.launch(FormUriActivity::class.java)
        onView(withText(R.string.app_not_configured)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When there is project id specified in uri and it does not match current project id then display alert dialog`() {
        saveTestProjects()

        val scenario = ActivityScenario.launch<FormUriActivity>(getIntent(secondProject.uuid))

        onView(withText(R.string.wrong_project_selected_for_form)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When there is project id specified in uri and it matches current project id then start form filling`() {
        saveTestProjects()

        ActivityScenario.launch<FormUriActivity>(getIntent(firstProject.uuid))

        Intents.intended(hasComponent(FormEntryActivity::class.java.name))
        Intents.intended(hasData(FormsContract.getUri(firstProject.uuid, 1)))
        Intents.intended(hasExtra("KEY_1", "Text"))
    }

    @Test
    fun `When there is no project id specified in uri and first available project id does not match current project id then display alert dialog`() {
        saveTestProjects()

        whenever(currentProjectProvider.getCurrentProject()).thenReturn(secondProject)

        val scenario = ActivityScenario.launch<FormUriActivity>(getIntent())

        onView(withText(R.string.wrong_project_selected_for_form)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When there is no project id specified in uri and first available project id matches current project id then start form filling`() {
        saveTestProjects()

        ActivityScenario.launch<FormUriActivity>(getIntent())

        Intents.intended(hasComponent(FormEntryActivity::class.java.name))
        val uri = FormsContract.getUri("", 1)
        Intents.intended(
            hasData(
                Uri.Builder()
                    .scheme(uri.scheme)
                    .authority(uri.authority)
                    .path(uri.path)
                    .query(null)
                    .build()
            )
        )
        Intents.intended(hasExtra("KEY_1", "Text"))
    }

    // TODO: Replace the explicit FormUriActivity intent with an implicit one Intent.ACTION_EDIT once it's possible https://github.com/android/android-test/issues/496
    private fun getIntent(projectId: String? = null) =
        Intent(context, FormUriActivity::class.java).apply {
            data = if (projectId == null) {
                val uri = FormsContract.getUri("", 1)
                Uri.Builder()
                    .scheme(uri.scheme)
                    .authority(uri.authority)
                    .path(uri.path)
                    .query(null)
                    .build()
            } else {
                FormsContract.getUri(projectId, 1)
            }
            putExtra("KEY_1", "Text")
        }

    private fun saveTestProjects() {
        projectsRepository.save(firstProject)
        projectsRepository.save(secondProject)

        whenever(currentProjectProvider.getCurrentProject()).thenReturn(firstProject)
    }
}
