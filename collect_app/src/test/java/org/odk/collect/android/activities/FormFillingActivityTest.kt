package org.odk.collect.android.activities

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formhierarchy.FormHierarchyFragmentHostActivity
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.CollectHelpers.resetProcess
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidtest.ActivityScenarioExtensions.isFinishing
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.forms.Form
import org.odk.collect.formstest.FormFixtures.form
import org.odk.collect.strings.R
import org.odk.collect.testshared.ActivityControllerRule
import org.odk.collect.testshared.AssertIntentsHelper
import org.odk.collect.testshared.Assertions.assertText
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.Interactions
import org.odk.collect.testshared.RobolectricHelpers.recreateWithProcessRestore
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class FormFillingActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    @get:Rule
    val activityControllerRule = ActivityControllerRule()

    @get:Rule
    val scenarioLauncherRule = ActivityScenarioLauncherRule()

    private val assertIntentsHelper = AssertIntentsHelper()

    private val scheduler = FakeScheduler()
    private val dependencies = object : AppDependencyModule() {
        override fun providesScheduler(workManager: WorkManager): Scheduler {
            return scheduler
        }
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var component: AppDependencyComponent

    @Before
    fun setup() {
        component = CollectHelpers.overrideAppDependencyModule(dependencies)
    }

    @Test
    fun whenProcessIsKilledAndRestored_returnsToHierarchyAtQuestion() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText(withText("Two Question"))
        assertText(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertText(withText("What is your age?"))

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText(withText("Two Question"))
        assertText(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andHierarchyIsOpen_returnsToHierarchyAtQuestion() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText(withText("Two Question"))
        assertText(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertText(withText("What is your age?"))

        Interactions.clickOn(withContentDescription(R.string.view_hierarchy))
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText(withText("Two Question"))
        assertText(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andThereADialogFragmentOpen_doesNotRestoreDialogFragment() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText(withText("Two Question"))
        assertText(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertText(withText("What is your age?"))

        val initialFragmentManager = initial.get().supportFragmentManager
        DialogFragmentUtils.showIfNotShowing(TestDialogFragment::class.java, initialFragmentManager)
        assertThat(
            initialFragmentManager.fragments.any { it::class == TestDialogFragment::class },
            equalTo(true)
        )

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText(withText("Two Question"))
        assertText(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andIsWaitingForExternalData_dataCanStillBeReturned() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question-external.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText(withText("Two Question"))
        assertText(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertText(withText("What is your age?"))

        // Open external app
        Interactions.clickOn(withContentDescription(R.string.launch_app))
        assertIntentsHelper.assertNewIntent(hasAction("com.example.EXAMPLE"))

        // Recreate with result
        val returnData = ExternalAppUtils.getReturnIntent("159")
        activityControllerRule.add {
            initial.recreateWithProcessRestore(RESULT_OK, returnData) { resetProcess(dependencies) }
        }

        scheduler.flush()

        assertIntentsHelper.assertNoNewIntent()
        assertText(withText("Two Question"))
        assertText(withText("What is your age?"))
        assertText(withText("159"))
    }

    /**
     * This case will usually be protected by [FormUriActivity], but it could be possible when
     * restoring the app/backstack.
     */
    @Test
    fun whenFormDoesNotExist_showsFatalError() {
        val projectId = CollectHelpers.setupDemoProject()

        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, 101),
            FormFillingActivity::class
        )

        val scenario = scenarioLauncherRule.launch<FormFillingActivity>(intent)
        scheduler.flush()
        assertText(
            withText("This form no longer exists, please email support@getodk.org with a description of what you were doing when this happened."),
            root = isDialog()
        )

        Interactions.clickOn(withText(R.string.ok), root = isDialog())
        assertThat(scenario.isFinishing, equalTo(true))
    }

    private fun setupForm(testFormPath: String): Form? {
        val formsDir = component.storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)
        val formFile = FileUtils.copyFileFromResources(
            testFormPath,
            File(formsDir, "two-question.xml")
        )

        val formsRepository = component.formsRepositoryProvider().create()
        val form = formsRepository.save(form(formFilePath = formFile.absolutePath))
        return form
    }

    class TestDialogFragment : DialogFragment()
}
