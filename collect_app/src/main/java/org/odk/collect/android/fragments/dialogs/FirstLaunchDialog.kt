package org.odk.collect.android.fragments.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.databinding.FirstLaunchDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.AddProjectDialog
import javax.inject.Inject

class FirstLaunchDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectImporter: ProjectImporter

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var binding: FirstLaunchDialogLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FirstLaunchDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appName.text = String.format(
            "%s %s",
            getString(R.string.app_name),
            versionInformation.versionToDisplay
        )

        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(
                AddProjectDialog::class.java,
                requireActivity().supportFragmentManager
            )
        }

        binding.configureLaterButton.setOnClickListener {
            projectImporter.importDemoProject()
            currentProjectProvider.setCurrentProject(ProjectImporter.DEMO_PROJECT_ID)

            ActivityUtils.startActivityAndCloseAllOthers(
                requireActivity(),
                MainMenuActivity::class.java
            )
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
