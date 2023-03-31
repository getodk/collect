package org.odk.collect.android.external

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class FormUriActivityTest {
    private lateinit var projectsRepository: ProjectsRepository
    private val currentProjectProvider = mock<CurrentProjectProvider>()
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val firstProject = Project.DEMO_PROJECT
    private val secondProject = Project.Saved("123", "Second project", "S", "#cccccc")
    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()
    private val formsRepositoryProvider = mock<FormsRepositoryProvider>().apply {
        whenever(get()).thenReturn(formsRepository)
    }
    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(get()).thenReturn(instancesRepository)
    }

    @get:Rule
    val activityRule = RecordedIntentsRule()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        projectsRepository = InMemProjectsRepository()

        formsRepository.save(
            FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )

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

            override fun providesFormsRepositoryProvider(application: Application?): FormsRepositoryProvider {
                return formsRepositoryProvider
            }

            override fun providesInstancesRepositoryProvider(
                context: Context?,
                storagePathProvider: StoragePathProvider?
            ): InstancesRepositoryProvider {
                return instancesRepositoryProvider
            }
        })
    }

    @Test
    fun `When there are no projects then display alert dialog`() {
        val scenario = launcherRule.launchForResult(FormUriActivity::class.java)
        onView(withText(R.string.app_not_configured)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When there is project id specified in uri but it does not match current project id then display alert dialog`() {
        saveTestProjects()

        val scenario = launcherRule.launchForResult<FormUriActivity>(getBlankFormIntent(secondProject.uuid))

        onView(withText(R.string.wrong_project_selected_for_form)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When there is no project id specified in uri and first available project id does not match current project id then display alert dialog`() {
        saveTestProjects()

        whenever(currentProjectProvider.getCurrentProject()).thenReturn(secondProject)

        val scenario = launcherRule.launchForResult<FormUriActivity>(getBlankFormIntent())

        onView(withText(R.string.wrong_project_selected_for_form)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When uri is null then display alert dialog`() {
        saveTestProjects()

        val scenario = launcherRule.launchForResult<FormUriActivity>(
            getBlankFormIntent().apply {
                data = null
            }
        )

        onView(withText(R.string.unrecognized_uri)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When uri is invalid then display alert dialog`() {
        saveTestProjects()

        val scenario = launcherRule.launchForResult<FormUriActivity>(
            getBlankFormIntent().apply {
                data = Uri.parse("blah")
            }
        )

        onView(withText(R.string.unrecognized_uri)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When uri represents a blank form that does not exist then display alert dialog`() {
        saveTestProjects()

        val scenario = launcherRule.launchForResult<FormUriActivity>(getBlankFormIntent(firstProject.uuid, 2))

        onView(withText(R.string.bad_uri)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `When uri represents a saved form that does not exist then display alert dialog`() {
        saveTestProjects()

        val scenario = launcherRule.launchForResult<FormUriActivity>(getSavedIntent(firstProject.uuid, 2))

        onView(withText(R.string.bad_uri)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
    }

    @Test
    fun `Form filling should not be started again after recreating the activity or getting back to it`() {
        saveTestProjects()

        val scenario = launcherRule.launch<FormUriActivity>(getBlankFormIntent(firstProject.uuid))

        scenario.recreate()

        Intents.intended(hasComponent(FormEntryActivity::class.java.name), Intents.times(1))
        Intents.intended(hasData(FormsContract.getUri(firstProject.uuid, 1)))
        Intents.intended(hasExtra("KEY_1", "Text"))
    }

    @Test
    fun `When there is project id specified in uri that represents a blank form and it matches current project id then start form filling`() {
        saveTestProjects()

        launcherRule.launch<FormUriActivity>(getBlankFormIntent(firstProject.uuid))

        Intents.intended(hasComponent(FormEntryActivity::class.java.name))
        Intents.intended(hasData(FormsContract.getUri(firstProject.uuid, 1)))
        Intents.intended(hasExtra("KEY_1", "Text"))
    }

    @Test
    fun `When there is project id specified in uri that represents a saved form and it matches current project id then start form filling`() {
        saveTestProjects()

        launcherRule.launch<FormUriActivity>(getSavedIntent(firstProject.uuid))

        Intents.intended(hasComponent(FormEntryActivity::class.java.name))
        Intents.intended(hasData(InstancesContract.getUri(firstProject.uuid, 1)))
        Intents.intended(hasExtra("KEY_1", "Text"))
    }

    @Test
    fun `When there is no project id specified in uri that represents a blank form and first available project id matches current project id then start form filling`() {
        saveTestProjects()

        launcherRule.launch<FormUriActivity>(getBlankFormIntent())

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

    @Test
    fun `When there is no project id specified in uri that represents a saved form and first available project id matches current project id then start form filling`() {
        saveTestProjects()

        launcherRule.launch<FormUriActivity>(getSavedIntent())

        Intents.intended(hasComponent(FormEntryActivity::class.java.name))
        val uri = InstancesContract.getUri("", 1)
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
    private fun getBlankFormIntent(projectId: String? = null, formDbId: Long = 1) =
        Intent(context, FormUriActivity::class.java).apply {
            data = if (projectId == null) {
                val uri = FormsContract.getUri("", formDbId)
                Uri.Builder()
                    .scheme(uri.scheme)
                    .authority(uri.authority)
                    .path(uri.path)
                    .query(null)
                    .build()
            } else {
                FormsContract.getUri(projectId, formDbId)
            }
            putExtra("KEY_1", "Text")
        }

    private fun getSavedIntent(projectId: String? = null, formDbId: Long = 1) =
        Intent(context, FormUriActivity::class.java).apply {
            data = if (projectId == null) {
                val uri = InstancesContract.getUri("", formDbId)
                Uri.Builder()
                    .scheme(uri.scheme)
                    .authority(uri.authority)
                    .path(uri.path)
                    .query(null)
                    .build()
            } else {
                InstancesContract.getUri(projectId, formDbId)
            }
            putExtra("KEY_1", "Text")
        }

    private fun saveTestProjects() {
        projectsRepository.save(firstProject)
        projectsRepository.save(secondProject)

        whenever(currentProjectProvider.getCurrentProject()).thenReturn(firstProject)
    }
}
