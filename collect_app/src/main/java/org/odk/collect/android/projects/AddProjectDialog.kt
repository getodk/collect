package org.odk.collect.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.odk.collect.android.R
import org.odk.collect.android.databinding.AddProjectDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class AddProjectDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    private lateinit var binding: AddProjectDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddProjectDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            projectsRepository.add(getProjectName())
            dismiss()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return binding.addProjectToolbar.toolbar
    }

    private fun setUpToolbar() {
        toolbar?.setTitle(R.string.add_project)
        toolbar?.navigationIcon = null
    }

    private fun getProjectName() = binding.projectName.editText?.text?.trim().toString()
}
