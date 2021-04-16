package org.odk.collect.android.fragments.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.first_launch_dialog_layout.*
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.AddProjectDialog
import org.odk.collect.android.projects.AddProjectDialog.Companion.STARTED_FROM_FIRST_LAUNCH_SCREEN
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class FirstLaunchDialog : MaterialFullScreenDialogFragment() {
    @Inject
    lateinit var projectImporter: ProjectImporter

    @Inject
    lateinit var versionInformation: VersionInformation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.first_launch_dialog_layout, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app_name.text = String.format("%s %s", getString(R.string.app_name), versionInformation.versionToDisplay)

        configure_manually_button.setOnClickListener {
            val bundle = Bundle().apply { putBoolean(STARTED_FROM_FIRST_LAUNCH_SCREEN, true) }
            DialogUtils.showIfNotShowing(AddProjectDialog::class.java, bundle, requireActivity().supportFragmentManager)
        }

        configure_later_button.setOnClickListener {
            projectImporter.importDemoProject()
            ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity::class.java)
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
    }

    override fun getToolbar(): Toolbar? {
        return null
    }
}
