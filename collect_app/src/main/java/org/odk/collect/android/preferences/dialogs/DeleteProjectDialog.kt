package org.odk.collect.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.FirstLaunchActivity
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.androidshared.ui.ToastUtils
import javax.inject.Inject

class DeleteProjectDialog : DialogFragment() {

    @Inject
    lateinit var projectDeleter: ProjectDeleter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(org.odk.collect.strings.R.string.delete_project)
            .setMessage(org.odk.collect.strings.R.string.delete_project_confirm_message)
            .setNegativeButton(org.odk.collect.strings.R.string.delete_project_no) { _: DialogInterface?, _: Int -> }
            .setPositiveButton(org.odk.collect.strings.R.string.delete_project_yes) { _: DialogInterface?, _: Int -> deleteProject() }
            .create()
    }

    private fun deleteProject() {
        Analytics.log(AnalyticsEvents.DELETE_PROJECT)

        when (val deleteProjectResult = projectDeleter.deleteProject()) {
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
                val newCurrentProject = deleteProjectResult.newCurrentProject
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
}
