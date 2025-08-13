package org.odk.collect.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
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
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance
import javax.inject.Inject

class DeleteProjectDialog : DialogFragment() {
    @Inject
    lateinit var projectDeleter: ProjectDeleter

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var scheduler: Scheduler

    lateinit var binding: DeleteProjectDialogLayoutBinding

    private val viewModel: DeleteProjectViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeleteProjectViewModel(
                    projectDeleter,
                    projectsDataService,
                    formsRepositoryProvider,
                    instancesRepositoryProvider,
                    scheduler
                ) as T
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DeleteProjectDialogLayoutBinding.inflate(layoutInflater).apply {
            cancelButton.setOnClickListener { dismiss() }
            deleteButton.setOnClickListener { viewModel.deleteProject() }

            confirmationFieldInput.doAfterTextChanged { text ->
                deleteButton.isEnabled = "delete".equals(text.toString().trim(), true)
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
            val message = getString(
                org.odk.collect.strings.R.string.delete_project_dialog_message,
                projectData.numberOfForms,
                projectData.numberOfSentForms,
                projectData.numberOfUnsentForms
            )
            binding.message.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        viewModel.deleteProjectResult.observe(this) { result ->
            dismiss()
            when (result) {
                is DeleteProjectResult.UnsentInstances -> {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(org.odk.collect.strings.R.string.cannot_delete_project_title)
                        .setMessage(org.odk.collect.strings.R.string.cannot_delete_project_message_one)
                        .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                        .show()
                }
                is DeleteProjectResult.RunningBackgroundJobs -> {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(org.odk.collect.strings.R.string.cannot_delete_project_title)
                        .setMessage(org.odk.collect.strings.R.string.cannot_delete_project_message_two)
                        .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                        .show()
                }
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

    class DeleteProjectViewModel(
        private val projectDeleter: ProjectDeleter,
        private val projectsDataService: ProjectsDataService,
        private val formsRepositoryProvider: FormsRepositoryProvider,
        private val instancesRepositoryProvider: InstancesRepositoryProvider,
        scheduler: Scheduler
    ) : ViewModel() {
        private val trackableWorker = TrackableWorker(scheduler)

        val isWorking = trackableWorker.isWorking

        private val _projectData = MutableLiveData<ProjectData>()
        val projectData: LiveData<ProjectData> = _projectData

        private val _deleteProjectResult = MutableLiveData<DeleteProjectResult>()
        val deleteProjectResult: LiveData<DeleteProjectResult> = _deleteProjectResult

        init {
            trackableWorker.immediate {
                val project = projectsDataService.getCurrentProject().value!!
                val numberOfForms = formsRepositoryProvider.create(project.uuid).all.size
                val instancesRepository = instancesRepositoryProvider.create(project.uuid)
                val numberOfSentForms = instancesRepository.getCountByStatus(Instance.STATUS_SUBMITTED)
                val numberOfUnsentForms = instancesRepository.getCountByStatus(
                    Instance.STATUS_INCOMPLETE,
                    Instance.STATUS_INVALID,
                    Instance.STATUS_VALID,
                    Instance.STATUS_COMPLETE,
                    Instance.STATUS_NEW_EDIT,
                    Instance.STATUS_SUBMISSION_FAILED,
                )
                _projectData.postValue(
                    ProjectData(
                        project.name,
                        numberOfForms,
                        numberOfSentForms,
                        numberOfUnsentForms
                    )
                )
            }
        }

        fun deleteProject() {
            Analytics.log(AnalyticsEvents.DELETE_PROJECT)
            trackableWorker.immediate {
                val result = projectDeleter.deleteProject()
                _deleteProjectResult.postValue(result)
            }
        }

        data class ProjectData(
            val projectName: String,
            val numberOfForms: Int,
            val numberOfSentForms: Int,
            val numberOfUnsentForms: Int
        )
    }
}
