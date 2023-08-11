package org.odk.collect.android.activities

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.application.Collect
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.formstest.FormFixtures.form
import org.odk.collect.testshared.FakeScheduler
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class FormFillingActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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

        val formsDir = component.storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)
        val formFile = FileUtils.copyFileFromResources(
            "forms/two-question.xml",
            File(formsDir, "two-question.xml")
        )

        val formsRepository = component.formsRepositoryProvider().get()
        val form = formsRepository.save(form(formFile = formFile))

        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()

        onView(withText("Two Question")).check(matches(isDisplayed()))
        onView(withText("What is your name?")).check(matches(isDisplayed()))

        onView(withText(org.odk.collect.strings.R.string.form_forward)).perform(click())
        scheduler.flush()
        onView(withText("What is your age?")).check(matches(isDisplayed()))

        // Destroy activity with saved instance state
        val outState = Bundle()
        initial.saveInstanceState(outState).pause().stop().destroy()

        // Reset process
        ApplicationProvider.getApplicationContext<Collect>().getState().clear()
        val newComponent = CollectHelpers.overrideAppDependencyModule(dependencies)
        newComponent.applicationInitializer().initialize()

        // Recreate and assert we start FormHierarchyActivity
        val recreated =
            Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup(outState)
        scheduler.flush()

        assertThat(
            shadowOf(initial.get()).nextStartedActivity.component,
            equalTo(ComponentName(application, FormHierarchyActivity::class.java))
        )

        // Return to FormFillingActivity from FormHierarchyActivity
        recreated.recreate().get()
            .onActivityResult(RequestCodes.HIERARCHY_ACTIVITY, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        onView(withText("Two Question")).check(matches(isDisplayed()))
        onView(withText("What is your age?")).check(matches(isDisplayed()))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andThereADialogFragmentOpen_doesNotRestoreDialogFragment() {
        val projectId = CollectHelpers.setupDemoProject()

        val formsDir = component.storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)
        val formFile = FileUtils.copyFileFromResources(
            "forms/two-question.xml",
            File(formsDir, "two-question.xml")
        )

        val formsRepository = component.formsRepositoryProvider().get()
        val form = formsRepository.save(form(formFile = formFile))

        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup()
        scheduler.flush()

        onView(withText("Two Question")).check(matches(isDisplayed()))
        onView(withText("What is your name?")).check(matches(isDisplayed()))

        onView(withText(org.odk.collect.strings.R.string.form_forward)).perform(click())
        scheduler.flush()
        onView(withText("What is your age?")).check(matches(isDisplayed()))

        val initialFragmentManager = initial.get().supportFragmentManager
        DialogFragmentUtils.showIfNotShowing(TestDialogFragment::class.java, initialFragmentManager)
        assertThat(
            initialFragmentManager.fragments.any { it::class == TestDialogFragment::class },
            equalTo(true)
        )

        // Destroy activity with saved instance state
        val outState = Bundle()
        initial.saveInstanceState(outState).pause().stop().destroy()

        // Reset process
        ApplicationProvider.getApplicationContext<Collect>().getState().clear()
        val newComponent = CollectHelpers.overrideAppDependencyModule(dependencies)
        newComponent.applicationInitializer().initialize()

        // Recreate and assert we start FormHierarchyActivity
        val recreated =
            Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup(outState)
        scheduler.flush()

        assertThat(
            shadowOf(initial.get()).nextStartedActivity.component,
            equalTo(ComponentName(application, FormHierarchyActivity::class.java))
        )

        // Return to FormFillingActivity from FormHierarchyActivity
        recreated.recreate().get()
            .onActivityResult(RequestCodes.HIERARCHY_ACTIVITY, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertThat(
            recreated.get().supportFragmentManager.fragments.any { it::class == TestDialogFragment::class },
            equalTo(false)
        )

        onView(withText("Two Question")).check(matches(isDisplayed()))
        onView(withText("What is your age?")).check(matches(isDisplayed()))
    }
}

class TestDialogFragment : DialogFragment()
