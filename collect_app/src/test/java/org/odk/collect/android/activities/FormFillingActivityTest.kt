package org.odk.collect.android.activities

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.application.Collect
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.formstest.FormUtils.buildForm
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class FormFillingActivityTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val dependencies = object : AppDependencyModule() {}

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(dependencies)
    }

    @Test
    fun whenProcessIsKilledAndRestoredDuringFormEntry_returnsToHierarchy() {
        val projectId = CollectHelpers.setupDemoProject()

        val formsRepository = DaggerUtils.getComponent(application).formsRepositoryProvider().get()
        val storagePathProvider = DaggerUtils.getComponent(application).storagePathProvider()
        val formsDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS)
        val newForm =
            buildForm(formId = "id", version = "version", formFilesPath = formsDir).build()
        val form = formsRepository.save(newForm)

        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )

        // Start activity
        val initial = Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup()
        onView(withText("Test Form")).check(matches(isDisplayed()))
        onView(withText("question label")).check(matches(isDisplayed()))

        // Destroy activity with saved instance state
        val outState = Bundle()
        initial.saveInstanceState(outState).pause().stop().destroy()

        // Reset process
        ApplicationProvider.getApplicationContext<Collect>().getState().clear()
        val newComponent = CollectHelpers.overrideAppDependencyModule(dependencies)
        newComponent.applicationInitializer().initialize()

        // Recreate and assert we start FormHierarchyActivity
        val recreated = Robolectric.buildActivity(FormFillingActivity::class.java, intent).setup()
        assertThat(
            shadowOf(initial.get()).nextStartedActivity.component,
            equalTo(ComponentName(application, FormHierarchyActivity::class.java))
        )

        // Return to FormFillingActivity from FormHierarchyActivity
        recreated.recreate().get()
            .onActivityResult(RequestCodes.HIERARCHY_ACTIVITY, Activity.RESULT_CANCELED, null)
        onView(withText("Test Form")).check(matches(isDisplayed()))
        onView(withText("question label")).check(matches(isDisplayed()))
    }
}
