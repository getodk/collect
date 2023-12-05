package org.odk.collect.android.external

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.activities.FormFillingActivity
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.android.instancemanagement.canBeEdited
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.ContentUriHelper
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import java.io.File
import javax.inject.Inject

/**
 * This class serves as a firewall for starting form filling. It should be used to do that
 * rather than [FormFillingActivity] directly as it ensures that the required data is valid.
 */
class FormUriActivity : ComponentActivity() {

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instanceRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var scheduler: Scheduler

    private var formFillingAlreadyStarted = false

    private val openForm =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(it.resultCode, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        scheduler.immediate(
            background = {
                assertProjectListNotEmpty() ?: assertCurrentProjectUsed() ?: assertValidUri()
                    ?: assertFormExists() ?: assertFormNotEncrypted()
            },
            foreground = { error ->
                if (error != null) {
                    displayErrorDialog(error)
                } else {
                    if (savedInstanceState != null) {
                        if (!savedInstanceState.getBoolean(FORM_FILLING_ALREADY_STARTED)) {
                            startForm()
                        }
                    } else {
                        startForm()
                    }
                }
            }
        )
    }

    private fun assertProjectListNotEmpty(): String? {
        val projects = projectsRepository.getAll()
        return if (projects.isEmpty()) {
            getString(string.app_not_configured)
        } else {
            null
        }
    }

    private fun assertCurrentProjectUsed(): String? {
        val projects = projectsRepository.getAll()
        val firstProject = projects.first()
        val uriProjectId = intent.data?.getQueryParameter("projectId")
        val projectId = uriProjectId ?: firstProject.uuid

        return if (projectId != projectsDataService.getCurrentProject().uuid) {
            getString(string.wrong_project_selected_for_form)
        } else {
            null
        }
    }

    private fun assertValidUri(): String? {
        val isUriValid = intent.data?.let {
            val uriMimeType = contentResolver.getType(it)
            if (uriMimeType == null) {
                false
            } else {
                uriMimeType == FormsContract.CONTENT_ITEM_TYPE || uriMimeType == InstancesContract.CONTENT_ITEM_TYPE
            }
        } ?: false

        return if (!isUriValid) {
            getString(string.unrecognized_uri)
        } else {
            null
        }
    }

    private fun assertFormExists(): String? {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        return if (uriMimeType == FormsContract.CONTENT_ITEM_TYPE) {
            val formExists = formsRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))?.let {
                File(it.formFilePath).exists()
            } ?: false

            if (formExists) {
                null
            } else {
                getString(string.bad_uri)
            }
        } else {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            if (instance == null) {
                getString(string.bad_uri)
            } else if (!File(instance.instanceFilePath).exists()) {
                Analytics.log(AnalyticsEvents.OPEN_DELETED_INSTANCE)
                InstanceDeleter(
                    instanceRepositoryProvider.get(),
                    formsRepositoryProvider.get()
                ).delete(instance.dbId)
                getString(string.instance_deleted_message)
            } else {
                val candidateForms = formsRepositoryProvider.get()
                    .getAllByFormIdAndVersion(instance.formId, instance.formVersion)
                if (candidateForms.isEmpty()) {
                    val version = if (instance.formVersion == null) {
                        ""
                    } else {
                        "\n${getString(string.version)} ${instance.formVersion}"
                    }

                    getString(string.parent_form_not_present, "${instance.formId}$version")
                } else if (candidateForms.size > 1) {
                    getString(string.survey_multiple_forms_error)
                } else {
                    null
                }
            }
        }
    }

    private fun assertFormNotEncrypted(): String? {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        return if (uriMimeType == InstancesContract.CONTENT_ITEM_TYPE) {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            if (instance!!.canEditWhenComplete()) {
                null
            } else {
                getString(string.encrypted_form)
            }
        } else {
            null
        }
    }

    private fun startForm() {
        formFillingAlreadyStarted = true
        openForm.launch(
            Intent(this, FormFillingActivity::class.java).apply {
                action = intent.action
                data = intent.data
                intent.extras?.let { sourceExtras -> putExtras(sourceExtras) }
                if (!canFormBeEdited()) {
                    putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT)
                }
            }
        )
    }

    private fun displayErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(string.ok) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
            .show()
    }

    private fun canFormBeEdited(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        val formEditingEnabled = if (uriMimeType == InstancesContract.CONTENT_ITEM_TYPE) {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            instance!!.canBeEdited(settingsProvider)
        } else {
            true
        }

        return formEditingEnabled
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FORM_FILLING_ALREADY_STARTED, formFillingAlreadyStarted)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val FORM_FILLING_ALREADY_STARTED = "FORM_FILLING_ALREADY_STARTED"
    }
}
