package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.ActivityHelpers
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Companion.DEMO_PROJECT
import java.io.IOException

class FormActivityTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : ActivityScenarioLauncherRule() {

    private lateinit var scenario: ActivityScenario<Activity>

    override fun before() {
        super.before()
        setUpProjectAndCopyForm()
        scenario = launch(activityIntent)
    }

    fun startInFormEntry(): FormEntryPage {
        return FormEntryPage(formName).assertOnPage()
    }

    fun startInFormHierarchy(): FormHierarchyPage {
        return FormHierarchyPage(formName).assertOnPage()
    }

    fun saveInstanceStateForActivity(): FormActivityTestRule {
        scenario.onActivity {
            it.onSaveInstanceState(Bundle(), PersistableBundle())
        }

        return this
    }

    fun destroyActivity(): FormActivityTestRule {
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

    fun reset(): FormActivityTestRule {
        CollectHelpers.simulateProcessRestart()
        scenario = launch(activityIntent)
        return this
    }

    private fun setUpProjectAndCopyForm() {
        try {
            // Set up demo project
            val component =
                DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
            component.projectsRepository().save(DEMO_PROJECT)
            component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
            StorageUtils.copyFormToDemoProject(formFilename, mediaFilePaths, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val activityIntent: Intent
        get() {
            val application = ApplicationProvider.getApplicationContext<Application>()
            val formPath = DaggerUtils.getComponent(application).storagePathProvider()
                .getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename
            val form = DaggerUtils.getComponent(application).formsRepositoryProvider().get()
                .getOneByPath(formPath)
            val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
                .getCurrentProject().uuid
            val intent = Intent(application, FormEntryActivity::class.java)
            intent.data = FormsContract.getUri(projectId, form!!.dbId)
            return intent
        }
}
