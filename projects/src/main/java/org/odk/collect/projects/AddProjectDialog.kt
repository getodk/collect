package org.odk.collect.projects

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.jetbrains.annotations.TestOnly
import org.odk.collect.androidshared.OneSignTextWatcher
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.databinding.AddProjectDialogLayoutBinding

class AddProjectDialog : MaterialFullScreenDialogFragment() {

    companion object {
        const val IS_PROJECT_NAME_SET_BY_USER = "IS_PROJECT_NAME_SET_BY_USER"
        const val IS_PROJECT_ICON_SET_BY_USER = "IS_PROJECT_ICON_SET_BY_USER"
        const val VALUE_CHANGED_PROGRAMMATICALLY = "VALUE_CHANGED_PROGRAMMATICALLY"
    }

    private lateinit var binding: AddProjectDialogLayoutBinding

    private var listener: AddProjectDialogListener? = null

    private var projectNameSetByUser = false

    private var projectIconSetByUser = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is AddProjectDialogListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectNameSetByUser = savedInstanceState?.getBoolean(IS_PROJECT_NAME_SET_BY_USER, false) ?: false
        projectIconSetByUser = savedInstanceState?.getBoolean(IS_PROJECT_ICON_SET_BY_USER, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddProjectDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.urlInputText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                generateNameAndIcon()
            }
        }

        binding.urlInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                generateNameAndIcon()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.projectNameInputText.post {
            binding.projectNameInputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (binding.projectNameInputText.tag != VALUE_CHANGED_PROGRAMMATICALLY) {
                        projectNameSetByUser = true
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        binding.projectIconInputText.post {
            binding.projectIconInputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (binding.projectIconInputText.tag != VALUE_CHANGED_PROGRAMMATICALLY) {
                        projectIconSetByUser = true
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        binding.projectIconInputText.addTextChangedListener(OneSignTextWatcher(binding.projectIconInputText))

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            val newProject = NewProject(getUrl(), getUsername(), getPassword(), getProjectName(), getProjectIcon(), getProjectColor())
            listener?.onProjectAdded(newProject)
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PROJECT_NAME_SET_BY_USER, projectNameSetByUser)
        outState.putBoolean(IS_PROJECT_ICON_SET_BY_USER, projectIconSetByUser)
    }

    private fun generateNameAndIcon() {
        val projectNameAndIcon = ProjectDetailsGenerator.getProjectNameAndIconFromUrl(getUrl())

        if (!projectNameSetByUser) {
            binding.projectNameInputText.tag = VALUE_CHANGED_PROGRAMMATICALLY
            binding.projectNameInputText.setText(projectNameAndIcon.first)
            binding.projectNameInputText.tag = ""
        }

        if (!projectIconSetByUser) {
            binding.projectIconInputText.tag = VALUE_CHANGED_PROGRAMMATICALLY
            binding.projectIconInputText.setText(projectNameAndIcon.second)
            binding.projectIconInputText.tag = ""
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar?.setTitle(R.string.add_project)
        toolbar?.navigationIcon = null
    }

    private fun getUrl() = binding.url.editText?.text?.trim().toString()

    private fun getUsername() = binding.username.editText?.text?.trim().toString()

    private fun getPassword() = binding.password.editText?.text?.trim().toString()

    private fun getProjectName() = binding.projectName.editText?.text?.trim().toString()

    private fun getProjectIcon() = binding.projectIcon.editText?.text?.trim().toString()

    private fun getProjectColor() = binding.projectColor.editText?.text?.trim().toString()

    @TestOnly
    fun setAddProjectDialogListener(listener: AddProjectDialogListener) {
        this.listener = listener
    }

    interface AddProjectDialogListener {
        fun onProjectAdded(newProject: NewProject)
    }
}
