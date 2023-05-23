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
import org.odk.collect.android.activities.FormFillingActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.androidshared.system.SavedInstanceStateProvider
import org.odk.collect.projects.Project
import timber.log.Timber
import java.io.IOException

class FormEntryActivityTestRule : ExternalResource() {

    private lateinit var intent: Intent
    private lateinit var scenario: ActivityScenarioWrapper

    private val savedInstanceStateProvider = InMemSavedInstanceStateProvider()

    override fun before() {
        super.before()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSavedInstanceStateProvider(): SavedInstanceStateProvider {
                return savedInstanceStateProvider
            }
        })
    }

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
        intent = createNewFormIntent(formFilename)
        scenario = ActivityScenarioWrapper.launch(intent)
        return destination.assertOnPage()
    }

    fun fillNewForm(formFilename: String, formName: String): FormEntryPage {
        return fillNewForm(formFilename, FormEntryPage(formName))
    }

    fun editForm(formFilename: String, instanceName: String): FormHierarchyPage {
        intent = createEditFormIntent(formFilename)
        scenario = ActivityScenarioWrapper.launch(intent)
        return FormHierarchyPage(instanceName).assertOnPage()
    }

    fun navigateAwayFromActivity(): FormEntryActivityTestRule {
        scenario.moveToState(Lifecycle.State.STARTED)
        scenario.saveInstanceState()
        return this
    }

    fun destroyActivity(): FormEntryActivityTestRule {
        scenario.moveToState(Lifecycle.State.DESTROYED)
        return this
    }

    fun restoreActivity() {
        savedInstanceStateProvider.setState(scenario.getSavedState())
        scenario.relaunch()
    }

    private fun createNewFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename
        val form = DaggerUtils.getComponent(application).formsRepositoryProvider().get()
            .getOneByPath(formPath)
        val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
            .getCurrentProject().uuid

        return FormFillingIntentFactory.newInstanceIntent(
            application,
            FormsContract.getUri(projectId, form!!.dbId),
            FormFillingActivity::class
        )
    }

    private fun createEditFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename
        val form = DaggerUtils.getComponent(application).formsRepositoryProvider().get()
            .getOneByPath(formPath)
        val instance = DaggerUtils.getComponent(application).instancesRepositoryProvider().get()
            .getAllByFormId(form!!.formId).first()
        val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
            .getCurrentProject().uuid

        return FormFillingIntentFactory.editInstanceIntent(
            application,
            projectId,
            instance.dbId,
            FormFillingActivity::class
        )
    }
}

private class ActivityScenarioWrapper private constructor(private var intent: Intent) {

    private var outState: Bundle? = null
    private var scenario: ActivityScenario<Activity> = ActivityScenario.launch(intent)

    fun moveToState(newState: Lifecycle.State) {
        scenario.moveToState(newState)
    }

    fun relaunch() {
        scenario = ActivityScenario.launch(intent)
    }

    fun saveInstanceState() {
        val bundle = Bundle()
        scenario.onActivity { it.onSaveInstanceState(bundle, PersistableBundle()) }
        outState = bundle
    }

    fun close() {
        scenario.close()
    }

    fun getSavedState(): Bundle? {
        return outState
    }

    companion object {
        fun launch(intent: Intent): ActivityScenarioWrapper {
            return ActivityScenarioWrapper(intent)
        }
    }
}

class InMemSavedInstanceStateProvider : SavedInstanceStateProvider {

    private var bundle: Bundle? = null

    fun setState(savedInstanceState: Bundle?) {
        bundle = savedInstanceState
    }

    override fun getState(savedInstanceState: Bundle?): Bundle? {
        return if (bundle != null) {
            bundle.also {
                bundle = null
            }
        } else {
            savedInstanceState
        }
    }
}
