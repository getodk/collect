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
import org.odk.collect.android.utilities.ContentUriHelper
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

/**
 * This class serves as a firewall for starting form filling. It should be used to do that
 * rather than [FormEntryActivity] directly as it ensures that the required data is valid.
 */
class FormUriActivity : ComponentActivity() {

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instanceRepositoryProvider: InstancesRepositoryProvider

    private var formFillingAlreadyStarted = false

    private val openForm =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(it.resultCode, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        when {
            !assertProjectListNotEmpty() -> Unit
            !assertCurrentProjectUsed() -> Unit
            !assertValidUri() -> Unit
            !assertFormExists() -> Unit
            !assertFormCanBeEdited() -> Unit
            !assertFormFillingNotAlreadyStarted(savedInstanceState) -> Unit
            else -> startForm()
        }
    }

    private fun assertProjectListNotEmpty(): Boolean {
        val projects = projectsRepository.getAll()
        return if (projects.isEmpty()) {
            displayErrorDialog(R.string.app_not_configured)
            false
        } else {
            true
        }
    }

    private fun assertCurrentProjectUsed(): Boolean {
        val projects = projectsRepository.getAll()
        val firstProject = projects.first()
        val uriProjectId = intent.data?.getQueryParameter("projectId")
        val projectId = uriProjectId ?: firstProject.uuid

        return if (projectId != currentProjectProvider.getCurrentProject().uuid) {
            displayErrorDialog(R.string.wrong_project_selected_for_form)
            false
        } else {
            true
        }
    }

    private fun assertValidUri(): Boolean {
        val isUriValid = intent.data?.let {
            val uriMimeType = contentResolver.getType(it)
            if (uriMimeType == null) {
                return@let false
            } else {
                return@let uriMimeType == FormsContract.CONTENT_ITEM_TYPE || uriMimeType == InstancesContract.CONTENT_ITEM_TYPE
            }
        } ?: false

        return if (!isUriValid) {
            displayErrorDialog(R.string.unrecognized_uri)
            false
        } else {
            true
        }
    }

    private fun assertFormExists(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        val doesFormExist = if (uriMimeType == FormsContract.CONTENT_ITEM_TYPE) {
            formsRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri)) != null
        } else {
            instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri)) != null
        }

        return if (!doesFormExist) {
            displayErrorDialog(R.string.bad_uri)
            false
        } else {
            true
        }
    }

    private fun assertFormCanBeEdited(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        val formBlankOrIncomplete = if (uriMimeType == InstancesContract.CONTENT_ITEM_TYPE) {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            instance!!.status == Instance.STATUS_INCOMPLETE
        } else {
            true
        }

        return if (!formBlankOrIncomplete) {
            displayErrorDialog(R.string.form_complete)
            false
        } else {
            true
        }
    }

    private fun assertFormFillingNotAlreadyStarted(savedInstanceState: Bundle?): Boolean {
        if (savedInstanceState != null) {
            formFillingAlreadyStarted = savedInstanceState.getBoolean(FORM_FILLING_ALREADY_STARTED)
        }
        return !formFillingAlreadyStarted
    }

    private fun startForm() {
        formFillingAlreadyStarted = true
        openForm.launch(
            Intent(this, FormEntryActivity::class.java).also {
                it.action = intent.action
                it.data = intent.data
                intent.extras?.let { sourceExtras -> it.putExtras(sourceExtras) }
            }
        )
    }

    private fun displayErrorDialog(message: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .create()
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FORM_FILLING_ALREADY_STARTED, formFillingAlreadyStarted)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val FORM_FILLING_ALREADY_STARTED = "FORM_FILLING_ALREADY_STARTED"
    }
}
