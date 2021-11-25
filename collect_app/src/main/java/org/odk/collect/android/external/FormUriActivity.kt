package org.odk.collect.android.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class FormUriActivity : Activity() {

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        val firstProject = projectsRepository.getAll().first()
        val uri = intent.data
        val uriProjectId = uri?.getQueryParameter("projectId")
        val projectId = uriProjectId ?: firstProject.uuid

        logAnalytics(uriProjectId)

        if (projectId == currentProjectProvider.getCurrentProject().uuid) {
            startActivity(
                Intent(this, FormEntryActivity::class.java).also {
                    it.data = uri
                    intent.extras?.let { sourceExtras -> it.putExtras(sourceExtras) }
                }
            )
        } else {
            AlertDialog.Builder(this)
                .setMessage(R.string.wrong_project_selected_for_form)
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .create()
                .show()
        }
    }

    private fun logAnalytics(uriProjectId: String?) {
        if (uriProjectId != null) {
            Analytics.log(AnalyticsEvents.FORM_ACTION_WITH_PROJECT_ID)
        } else {
            Analytics.log(AnalyticsEvents.FORM_ACTION_WITHOUT_PROJECT_ID)
        }

        if (intent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE) != null) {
            Analytics.log(AnalyticsEvents.FORM_ACTION_WITH_FORM_MODE_EXTRA)
        }
    }
}
