package org.odk.collect.android.external

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class FormUriActivity : ComponentActivity() {

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    private var formFillingAlreadyStarted = false

    private val openForm =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(it.resultCode, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        if (savedInstanceState != null) {
            formFillingAlreadyStarted = savedInstanceState.getBoolean(FORM_FILLING_ALREADY_STARTED)
        }

        val projects = projectsRepository.getAll()
        if (projects.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.app_not_configured)
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .create()
                .show()
        } else {
            val firstProject = projects.first()
            val uri = intent.data
            val uriProjectId = uri?.getQueryParameter("projectId")
            val projectId = uriProjectId ?: firstProject.uuid

            if (projectId == currentProjectProvider.getCurrentProject().uuid) {
                if (!formFillingAlreadyStarted) {
                    formFillingAlreadyStarted = true
                    openForm.launch(
                        Intent(this, FormEntryActivity::class.java).also {
                            it.action = intent.action
                            it.data = uri
                            intent.extras?.let { sourceExtras -> it.putExtras(sourceExtras) }
                        },
                    )
                }
            } else {
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.wrong_project_selected_for_form)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        finish()
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FORM_FILLING_ALREADY_STARTED, formFillingAlreadyStarted)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val FORM_FILLING_ALREADY_STARTED = "FORM_FILLING_ALREADY_STARTED"
    }
}
