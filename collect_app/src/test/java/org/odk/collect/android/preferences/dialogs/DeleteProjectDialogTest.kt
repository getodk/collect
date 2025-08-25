package org.odk.collect.android.preferences.dialogs

import android.app.Application
import android.content.Context
import androidx.core.text.HtmlCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class DeleteProjectDialogTest {
    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private val projectDeleter = mock<ProjectDeleter>()
    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()
    private val scheduler = FakeScheduler()

    @Before
    fun setup() {
        val projectId = CollectHelpers.setupDemoProject()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectDeleter(
                projectsRepository: ProjectsRepository?,
                projectsDataService: ProjectsDataService?,
                formUpdateScheduler: FormUpdateScheduler?,
                instanceSubmitScheduler: InstanceSubmitScheduler?,
                storagePathProvider: StoragePathProvider?,
                settingsProvider: SettingsProvider?
            ): ProjectDeleter {
                return projectDeleter
            }

            override fun providesFormsRepositoryProvider(application: Application?): FormsRepositoryProvider {
                return mock<FormsRepositoryProvider>().apply {
                    whenever(create(projectId)).thenReturn(formsRepository)
                }
            }

            override fun providesInstancesRepositoryProvider(
                context: Context?,
                storagePathProvider: StoragePathProvider?
            ): InstancesRepositoryProvider {
                return mock<InstancesRepositoryProvider>().apply {
                    whenever(create(projectId)).thenReturn(instancesRepository)
                }
            }

            override fun providesScheduler(workManager: WorkManager?): Scheduler {
                return scheduler
            }
        })
    }

    @Test
    fun `The dialog is dismissed after pressing the device back button`() {
        val scenario = launcherRule.launch(DeleteProjectDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, equalTo(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.dialog, equalTo(null))
            verifyNoInteractions(projectDeleter)
        }
    }

    @Test
    fun `The dialog is dismissed after clicking the Cancel button`() {
        val scenario = launcherRule.launch(DeleteProjectDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, equalTo(true))
            onView(withText(org.odk.collect.strings.R.string.cancel)).inRoot(isDialog()).perform(click())
            assertThat(it.dialog, equalTo(null))
            verifyNoInteractions(projectDeleter)
        }
    }

    @Test
    fun `The Delete Project button becomes enabled after typing Delete`() {
        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.runBackground()
        scheduler.runForeground()

        onView(withText(org.odk.collect.strings.R.string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(not(isEnabled())))
        onView(withId(R.id.confirmation_field_input)).perform(replaceText("Blah"))
        onView(withText(org.odk.collect.strings.R.string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(not(isEnabled())))
        onView(withId(R.id.confirmation_field_input)).perform(replaceText("Delete"))
        onView(withText(org.odk.collect.strings.R.string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(isEnabled()))
    }

    @Test
    fun `The message shows the correct number of blank forms`() {
        formsRepository.save(
            FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build()
        )

        val message = HtmlCompat.fromHtml(
            context.getLocalizedString(org.odk.collect.strings.R.string.delete_project_dialog_message, 1, 0, 0, "", 0),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.runBackground()
        scheduler.runForeground()
        onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `The message shows the correct number of sent forms`() {
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Sent form", Instance.STATUS_SUBMITTED, null, TempFiles.createTempDir().absolutePath).build()
        )

        val message = HtmlCompat.fromHtml(
            context.getLocalizedString(org.odk.collect.strings.R.string.delete_project_dialog_message, 0, 1, 0, "", 0),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.runBackground()
        scheduler.runForeground()
        onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `The message shows the correct number of unsent forms and drafts`() {
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Submission failed form", Instance.STATUS_SUBMISSION_FAILED, null, TempFiles.createTempDir().absolutePath).build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Finalized form", Instance.STATUS_COMPLETE, null, TempFiles.createTempDir().absolutePath).build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Draft valid form", Instance.STATUS_INVALID, null, TempFiles.createTempDir().absolutePath).build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Draft invalid form", Instance.STATUS_VALID, null, TempFiles.createTempDir().absolutePath).build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Draft new edit form", Instance.STATUS_NEW_EDIT, null, TempFiles.createTempDir().absolutePath).build()
        )

        val message = HtmlCompat.fromHtml(
            context.getLocalizedString(org.odk.collect.strings.R.string.delete_project_dialog_message, 0, 0, 5, "⚠\uFE0F", 3),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.runBackground()
        scheduler.runForeground()
        onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }
}
