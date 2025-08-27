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
import org.mockito.Mockito.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InstanceUtils
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class DeleteProjectDialogTest {
    private val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
    private val projectDataService = component.currentProjectProvider()
    private val projectDeleter = mock<ProjectDeleter>()
    private val formsDataService = component.formsDataService()
    private val instancesDataService = component.instancesDataService()
    private val scheduler = FakeScheduler()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var projectId: String
    private lateinit var formsRepository: FormsRepository
    private lateinit var instancesRepository: InstancesRepository

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(DeleteProjectDialog::class) {
                DeleteProjectDialog(
                    projectDeleter,
                    projectDataService,
                    formsDataService,
                    instancesDataService,
                    scheduler
                )
            }.build()
    )

    @Before
    fun setup() {
        projectId = CollectHelpers.setupDemoProject()
        formsRepository = component.formsRepositoryProvider().create(projectId)
        instancesRepository = component.instancesRepositoryProvider().create(projectId)

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectDeleter(
                projectsRepository: ProjectsRepository,
                projectsDataService: ProjectsDataService,
                formUpdateScheduler: FormUpdateScheduler,
                instanceSubmitScheduler: InstanceSubmitScheduler,
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider
            ): ProjectDeleter {
                return projectDeleter
            }

            override fun providesScheduler(workManager: WorkManager): Scheduler {
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
            onView(withText(string.cancel)).inRoot(isDialog()).perform(click())
            assertThat(it.dialog, equalTo(null))
            verifyNoInteractions(projectDeleter)
        }
    }

    @Test
    fun `The Delete Project button becomes enabled after typing Delete`() {
        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()

        onView(withText(string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(not(isEnabled())))
        onView(withId(R.id.confirmation_field_input)).inRoot(isDialog()).perform(replaceText("Blah"))
        onView(withText(string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(not(isEnabled())))
        onView(withId(R.id.confirmation_field_input)).inRoot(isDialog()).perform(replaceText("Delete"))
        onView(withText(string.delete_project_confirm_button_text)).inRoot(isDialog()).check(matches(isEnabled()))
    }

    @Test
    fun `The ProjectDeleter is called after clicking the Delete button`() {
        whenever(projectDeleter.deleteProject(projectId)).thenReturn(DeleteProjectResult.DeletedSuccessfullyLastProject)

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()

        onView(withId(R.id.confirmation_field_input)).inRoot(isDialog()).perform(replaceText("Delete"))
        onView(withText(string.delete_project_confirm_button_text)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        verify(projectDeleter).deleteProject(projectId)
    }

    @Test
    fun `The message shows the correct number of blank forms`() {
        formsRepository.save(
            FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build()
        )

        val message = HtmlCompat.fromHtml(
            context.getLocalizedString(string.delete_project_dialog_message, 1, 0, 0, "", 0),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `The message shows the correct number of sent forms`() {
        instancesRepository.save(
            InstanceUtils.buildInstance("1", "1", "Sent form", Instance.STATUS_SUBMITTED, null, TempFiles.createTempDir().absolutePath).build()
        )

        val message = HtmlCompat.fromHtml(
            context.getLocalizedString(string.delete_project_dialog_message, 0, 1, 0, "", 0),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
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
            context.getLocalizedString(string.delete_project_dialog_message, 0, 0, 5, "⚠\uFE0F", 3),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }
}
