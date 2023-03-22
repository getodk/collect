package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.ExternalResource
import org.odk.collect.android.formmanagement.FormNavigator
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.ActivityHelpers
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.projects.Project
import timber.log.Timber
import java.io.IOException

class FormEntryActivityTestRule : ExternalResource() {

    private lateinit var intent: Intent
    private lateinit var scenario: ActivityScenario<Activity>

    override fun after() {
        try {
            scenario.close()
        } catch (e: Throwable) {
            Timber.e(Error("Error closing ActivityScenario: $e"))
        }
    }

    fun setUpProjectAndCopyForm(formFilename: String): FormEntryActivityTestRule {
        try {
            // Set up demo project
            val component =
                DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
            component.projectsRepository().save(Project.DEMO_PROJECT)
            component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
            StorageUtils.copyFormToDemoProject(formFilename, null, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    fun <D : Page<D>> fillNewForm(formFilename: String, destination: D): D {
        intent = createIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return destination.assertOnPage() as D
    }

    fun fillNewForm(formFilename: String, formName: String): FormEntryPage {
        return fillNewForm(formFilename, FormEntryPage(formName))
    }

    fun saveInstanceStateForActivity(): FormEntryActivityTestRule {
        scenario.onActivity {
            it.onSaveInstanceState(Bundle(), PersistableBundle())
        }

        return this
    }

    fun destroyActivity(): FormEntryActivityTestRule {
        lateinit var scenarioActivity: Activity
        scenario.onActivity {
            scenarioActivity = it
        }

        if (ActivityHelpers.getActivity() != scenarioActivity) {
            throw IllegalStateException("Can't destroy backstack!")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        return this
    }

    fun reset(): FormEntryActivityTestRule {
        CollectHelpers.simulateProcessRestart()
        return this
    }

    private fun createIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename
        val form = DaggerUtils.getComponent(application).formsRepositoryProvider().get()
            .getOneByPath(formPath)
        val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
            .getCurrentProject().uuid

        return FormNavigator.newInstanceIntent(application, projectId, form!!.dbId)
    }
}
