package org.odk.collect.android.projects

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.databinding.ProjectSettingsDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.CurrentProjectViewModel
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.android.wassan.activity.MainActivity
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Inject

class ProjectSettingsDialog(private val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var binding: ProjectSettingsDialogLayoutBinding

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    // Listener to delegate project switching to the hosting component
    // niranjan added
    var onProjectSwitchListener: ((Project.Saved) -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        currentProjectViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[CurrentProjectViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ProjectSettingsDialogLayoutBinding.inflate(LayoutInflater.from(context))

        currentProjectViewModel.currentProject.observe(this) {
            if (it != null) {
                binding.currentProject.setupView(it, settingsProvider.getUnprotectedSettings())
                binding.currentProject.contentDescription =
                    getString(org.odk.collect.strings.R.string.using_project, it.name)

                inflateListOfInActiveProjects(requireContext(), it)
            }
        }

        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        binding.generalSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProjectPreferencesActivity::class.java))
            dismiss()
        }

        binding.addProjectButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                QrCodeProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
            dismiss()
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
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

            projectView.setupView(project, settingsProvider.getUnprotectedSettings(project.uuid))
            projectView.contentDescription =
                getString(org.odk.collect.strings.R.string.switch_to_project, project.name)
            binding.projectList.addView(projectView)
        }
    }

    /*private fun switchProject(project: Project.Saved) {
        currentProjectViewModel.setCurrentProject(project)

        ActivityUtils.startActivityAndCloseAllOthers(
            requireActivity(),
            MainMenuActivity::class.java
        )
        ToastUtils.showLongToast(
            requireContext(),
            getString(org.odk.collect.strings.R.string.switched_project, project.name)
        )
        dismiss()
    }*/

    private fun switchProject(project: Project.Saved) {
        //niranjan added
        onProjectSwitchListener?.invoke(project) ?: run {
            // Default internal logic
            currentProjectViewModel.setCurrentProject(project)

            ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), MainActivity::class.java)
            ToastUtils.showLongToast(
                requireContext(),
                getString(org.odk.collect.strings.R.string.switched_project, project.name)
            )
            dismiss()
        }
    }
}
