package org.odk.collect.android.preferences.dialogs

import android.app.Application
import android.text.TextUtils
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.containsString
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
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.Assertions
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class DeleteProjectDialogTest {
    private val projectDataService = mock<ProjectsDataService>()
    private val projectDeleter = mock<ProjectDeleter>()
    private val formsDataService = mock<FormsDataService>()
    private val instancesDataService = mock<InstancesDataService>()
    private val scheduler = FakeScheduler()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var projectId: String

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
        whenever(projectDataService.getCurrentProject()).thenReturn(MutableStateFlow(Project.DEMO_PROJECT))
        whenever(formsDataService.getFormsCount(projectId)).thenReturn(MutableStateFlow(0))
        whenever(instancesDataService.getSuccessfullySentCount(projectId)).thenReturn(MutableStateFlow(0))
        whenever(instancesDataService.getSendableCount(projectId)).thenReturn(MutableStateFlow(0))
        whenever(instancesDataService.getEditableCount(projectId)).thenReturn(MutableStateFlow(0))

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
    fun `During data loading the loading spinner is displayed and other UI elements are disabled`() {
        launcherRule.launch(DeleteProjectDialog::class.java)

        onView(withId(R.id.progress_bar)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.title)).inRoot(isDialog()).check(matches(not(isDisplayed())))
        onView(withId(R.id.message)).inRoot(isDialog()).check(matches(not(isDisplayed())))
        onView(withId(R.id.confirmation_field)).inRoot(isDialog()).check(matches(not(isDisplayed())))
        onView(withId(R.id.delete_button)).inRoot(isDialog()).check(matches(not(isClickable())))

        scheduler.flush()

        onView(withId(R.id.progress_bar)).inRoot(isDialog()).check(matches(not(isDisplayed())))
        onView(withId(R.id.title)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.message)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.confirmation_field)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.delete_button)).inRoot(isDialog()).check(matches(isClickable()))
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
    fun `The title is single-lined with end ellipsize`() {
        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        onView(withId(R.id.title)).inRoot(isDialog()).check { view, _ ->
            val textView = view as MaterialTextView
            assertThat(textView.maxLines, equalTo(1))
            assertThat(textView.ellipsize, equalTo(TextUtils.TruncateAt.END))
        }
    }

    @Test
    fun `The message shows the correct number of blank forms`() {
        whenever(formsDataService.getFormsCount(projectId)).thenReturn(MutableStateFlow(1))

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        assertCounts(forms = 1, sent = 0, unsent = 0, drafts = 0)
    }

    @Test
    fun `The message shows the correct number of sent forms`() {
        whenever(instancesDataService.getSuccessfullySentCount(projectId)).thenReturn(MutableStateFlow(1))

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        assertCounts(forms = 0, sent = 1, unsent = 0, drafts = 0)
    }

    @Test
    fun `The message shows the correct number of unsent forms and drafts`() {
        whenever(instancesDataService.getSendableCount(projectId)).thenReturn(MutableStateFlow(5))
        whenever(instancesDataService.getEditableCount(projectId)).thenReturn(MutableStateFlow(3))

        launcherRule.launch(DeleteProjectDialog::class.java)
        scheduler.flush()
        assertCounts(forms = 0, sent = 0, unsent = 5, drafts = 3)
    }

    private fun assertCounts(forms: Int, sent: Int, unsent: Int, drafts: Int) {
        Assertions.assertVisible(
            view = withText(containsString(context.getString(string.form_definitions_count, forms))),
            root = isDialog()
        )
        Assertions.assertVisible(
            view = withText(containsString(context.getString(string.sent_count, sent))),
            root = isDialog()
        )
        Assertions.assertVisible(
            view = withText(containsString(context.getString(string.unsent_count, unsent))),
            root = isDialog()
        )
        Assertions.assertVisible(
            view = withText(containsString(context.getString(string.drafts_count, drafts))),
            root = isDialog()
        )
    }
}
