package org.odk.collect.android.project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class AddProjectDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_project_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        view.findViewById<MaterialButton>(R.id.cancel_button).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.add_button).setOnClickListener {
            dismiss()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    private fun setUpToolbar() {
        toolbar?.setTitle(R.string.add_project)
        toolbar?.navigationIcon = null
    }
}
