package org.odk.collect.android.projects

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.odk.collect.android.application.Collect
import org.odk.collect.android.databinding.ProjectSettingsDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity
import org.odk.collect.android.preferences.screens.GeneralPreferencesActivity
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.projects.AddProjectDialog
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class ProjectSettingsDialog : DialogFragment() {

    @Inject
    lateinit var adminPasswordProvider: AdminPasswordProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var currentProjectViewModelFactory: CurrentProjectViewModel.Factory

    lateinit var binding: ProjectSettingsDialogLayoutBinding

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        currentProjectViewModel = ViewModelProvider(
            requireActivity(),
            currentProjectViewModelFactory
        )[CurrentProjectViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ProjectSettingsDialogLayoutBinding.inflate(LayoutInflater.from(context))

        currentProjectViewModel.currentProject.observe(this) { project ->
            binding.currentProject.project = project
            binding.currentProject.contentDescription =
                getString(R.string.using_project, project.name)
            inflateListOfInActiveProjects(requireContext(), project)
        }

        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        binding.generalSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), GeneralPreferencesActivity::class.java))
            dismiss()
        }

        binding.adminSettingsButton.setOnClickListener {
            if (adminPasswordProvider.isAdminPasswordSet) {
                val args = Bundle().also {
                    it.putSerializable(
                        AdminPasswordDialogFragment.ARG_ACTION,
                        AdminPasswordDialogFragment.Action.ADMIN_SETTINGS
                    )
                }
                DialogUtils.showIfNotShowing(
                    AdminPasswordDialogFragment::class.java,
                    args,
                    requireActivity().supportFragmentManager
                )
            } else {
                startActivity(Intent(requireContext(), AdminPreferencesActivity::class.java))
            }
            dismiss()
        }

        binding.addProjectButton.setOnClickListener {
            DialogUtils.showIfNotShowing(
                AddProjectDialog::class.java,
                requireActivity().supportFragmentManager
            )
            dismiss()
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun inflateListOfInActiveProjects(context: Context, currentProject: Project.Saved) {
        if (projectsRepository.getAll().none { it.uuid != currentProject.uuid }) {
            binding.topDivider.visibility = INVISIBLE
        } else {
            binding.topDivider.visibility = VISIBLE
        }

        projectsRepository.getAll().filter {
            it.uuid != currentProject.uuid
        }.forEach { project ->
            val projectView = ProjectListItemView(context)

            projectView.setOnClickListener {
                switchProject(project)
            }

            projectView.project = project
            projectView.contentDescription = getString(R.string.switch_to_project, project.name)
            binding.projectList.addView(projectView)
        }
    }

    private fun switchProject(project: Project.Saved) {
        Collect.resetDatabaseConnections()
        currentProjectViewModel.setCurrentProject(project)

        ToastUtils.showLongToast(getString(R.string.switched_project, project.name))
        dismiss()
    }
}
