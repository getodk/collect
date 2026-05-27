package org.odk.collect.android.preferences.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.FirstLaunchActivity
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.databinding.DeleteProjectDialogLayoutBinding
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.async.Scheduler

class DeleteProjectDialog(
    private val projectDeleter: ProjectDeleter,
    private val projectsDataService: ProjectsDataService,
    private val formsDataService: FormsDataService,
    private val instancesDataService: InstancesDataService,
    private val scheduler: Scheduler
) : DialogFragment() {
    private val viewModel: DeleteProjectViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeleteProjectViewModel(
                    projectsDataService,
                    projectDeleter,
                    formsDataService,
                    instancesDataService,
                    scheduler
                ) as T
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DeleteProjectDialogLayoutBinding.inflate(layoutInflater).apply {
            cancelButton.setOnClickListener { dismiss() }
            deleteButton.setOnClickListener { viewModel.deleteProject() }

            confirmationFieldInput.doAfterTextChanged { text ->
                val deleteTrigger = getString(org.odk.collect.strings.R.string.delete_trigger)
                deleteButton.isEnabled = deleteTrigger.equals(text.toString().trim(), true)
            }
        }

        viewModel.isWorking.observe(this) { isWorking ->
            binding.progressBar.isVisible = isWorking
            binding.title.isVisible = !isWorking
            binding.message.isVisible = !isWorking
            binding.confirmationField.isVisible = !isWorking
            binding.deleteButton.isClickable = !isWorking
        }

        viewModel.projectData.observe(this) { projectData ->
            binding.title.text = getString(
                org.odk.collect.strings.R.string.delete_project_dialog_title,
                projectData.projectName
            )
            val message = createDeleteMessage(
                projectData.numberOfForms,
                projectData.numberOfSentForms,
                projectData.numberOfUnsentForms,
                projectData.numberOfDraftForms
            )
            binding.message.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        viewModel.deleteProjectResult.observe(this) { result ->
            dismiss()
            when (result) {
                is DeleteProjectResult.DeletedSuccessfullyCurrentProject -> {
                    val newCurrentProject = result.newCurrentProject
                    ActivityUtils.startActivityAndCloseAllOthers(
                        requireActivity(),
                        MainMenuActivity::class.java
                    )
                    ToastUtils.showLongToast(
                        getString(
                            org.odk.collect.strings.R.string.switched_project,
                            newCurrentProject.name
                        )
                    )
                }

                is DeleteProjectResult.DeletedSuccessfullyLastProject -> {
                    ActivityUtils.startActivityAndCloseAllOthers(
                        requireActivity(),
                        FirstLaunchActivity::class.java
                    )
                }

                is DeleteProjectResult.DeletedSuccessfullyInactiveProject -> {
                    // not possible here
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun createDeleteMessage(
        formDefinitionsCount: Int,
        sentCount: Int,
        unsentCount: Int,
        draftsCount: Int
    ): String {
        val message = getString(org.odk.collect.strings.R.string.delete_project_message)
        val formDefinitions =
            getString(org.odk.collect.strings.R.string.form_definitions_count, formDefinitionsCount)
        val sent = getString(org.odk.collect.strings.R.string.sent_count, sentCount)
        val drafts = getString(org.odk.collect.strings.R.string.drafts_count, draftsCount)
        val unsent = if (unsentCount > 0) {
            "${getString(org.odk.collect.strings.R.string.unsent_count, unsentCount)} ⚠\uFE0F"
        } else {
            getString(org.odk.collect.strings.R.string.unsent_count, unsentCount)
        }

        val instructions = getString(
            org.odk.collect.strings.R.string.delete_project_instructions,
            "<b>${getString(org.odk.collect.strings.R.string.delete_trigger)}</b>"
        )

        return """
        $message:<br/>
            <br/>
        • $formDefinitions<br/>
        • $sent<br/>
        • $unsent<br/>
        • $drafts<br/>
        <br/>
        $instructions
        """
    }

    class DeleteProjectViewModel(
        projectsDataService: ProjectsDataService,
        private val projectDeleter: ProjectDeleter,
        private val formsDataService: FormsDataService,
        private val instancesDataService: InstancesDataService,
        scheduler: Scheduler
    ) : ViewModel() {
        private val trackableWorker = TrackableWorker(scheduler)

        val isWorking = trackableWorker.isWorking

        private val _projectData = MutableLiveData<ProjectData>()
        val projectData: LiveData<ProjectData> = _projectData

        private val _deleteProjectResult = MutableLiveData<DeleteProjectResult>()
        val deleteProjectResult: LiveData<DeleteProjectResult> = _deleteProjectResult

        private val project = projectsDataService.getCurrentProject().value!!

        init {
            trackableWorker.immediate {
                formsDataService.update(project.uuid)
                instancesDataService.update(project.uuid)

                val numberOfForms = formsDataService.getFormsCount(project.uuid).value
                val numberOfSentForms =
                    instancesDataService.getSuccessfullySentCount(project.uuid).value
                val numberOfUnsentForms = instancesDataService.getSendableCount(project.uuid).value
                val numberOfDraftForms = instancesDataService.getEditableCount(project.uuid).value
                _projectData.postValue(
                    ProjectData(
                        project.name,
                        numberOfForms,
                        numberOfSentForms,
                        numberOfUnsentForms,
                        numberOfDraftForms
                    )
                )
            }
        }

        fun deleteProject() {
            Analytics.log(AnalyticsEvents.DELETE_PROJECT)
            trackableWorker.immediate {
                val result = projectDeleter.deleteProject(project.uuid)
                _deleteProjectResult.postValue(result)
            }
        }

        data class ProjectData(
            val projectName: String,
            val numberOfForms: Int,
            val numberOfSentForms: Int,
            val numberOfUnsentForms: Int,
            val numberOfDraftForms: Int
        )
    }
}
