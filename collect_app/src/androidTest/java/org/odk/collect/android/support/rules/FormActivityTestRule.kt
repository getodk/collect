package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.AdbFormLoadingUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Companion.DEMO_PROJECT
import java.io.IOException

class FormActivityTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : ActivityScenarioLauncherRule() {

    private var formEntryPage: FormEntryPage? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                setUpProjectAndCopyForm()
                launch<Activity>(activityIntent)
                formEntryPage = FormEntryPage(formName)
                formEntryPage!!.assertOnPage()
                base.evaluate()
            }
        }
    }

    fun startInFormEntry(): FormEntryPage? {
        return formEntryPage
    }

    private fun setUpProjectAndCopyForm() {
        try {
            // Set up demo project
            val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
            component.projectsRepository().save(DEMO_PROJECT)
            component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
            AdbFormLoadingUtils.copyFormToDemoProject(formFilename, mediaFilePaths, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val activityIntent: Intent
        get() {
            val application = ApplicationProvider.getApplicationContext<Application>()
            val formPath = DaggerUtils.getComponent(application).storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename
            val form = DaggerUtils.getComponent(application).formsRepositoryProvider().get().getOneByPath(formPath)
            val projectId = DaggerUtils.getComponent(application).currentProjectProvider().getCurrentProject().uuid
            val intent = Intent(application, FormEntryActivity::class.java)
            intent.data = FormsContract.getUri(projectId, form!!.dbId)
            return intent
        }
}
