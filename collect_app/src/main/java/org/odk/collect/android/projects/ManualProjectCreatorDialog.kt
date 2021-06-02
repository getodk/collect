package org.odk.collect.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import org.odk.collect.android.R
import org.odk.collect.android.databinding.ManualProjectCreatorDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectGenerator
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class ManualProjectCreatorDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    private lateinit var binding: ManualProjectCreatorDialogLayoutBinding

    private var listener: AddProjectDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        if (context is AddProjectDialogListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ManualProjectCreatorDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.urlInputText.doOnTextChanged { text, _, _, _ ->
            binding.addButton.isEnabled = !text.isNullOrBlank()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            val newProject = ProjectGenerator.generateProject(getUrl())
            val savedProject = projectsRepository.save(newProject)

            listener?.onProjectAdded(savedProject, getUrl(), getUsername(), getPassword())
            dismiss()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun getUrl() = binding.url.editText?.text?.trim().toString()

    private fun getUsername() = binding.username.editText?.text?.trim().toString()

    private fun getPassword() = binding.password.editText?.text?.trim().toString()

    interface AddProjectDialogListener {
        fun onProjectAdded(project: Project.Saved, url: String, username: String, password: String)
    }
}
