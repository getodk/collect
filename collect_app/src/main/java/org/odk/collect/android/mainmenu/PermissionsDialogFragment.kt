package org.odk.collect.android.mainmenu

import android.Manifest
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys

class PermissionsDialogFragment constructor(
    private val settingsProvider: SettingsProvider,
    private val permissionsProvider: PermissionsProvider,
    private val permissionChecker: PermissionsChecker
) : DialogFragment() {

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            dismiss()
        } else {
            val permissionsAlreadyRequested =
                settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED)
            val permissionsGranted = permissionChecker.isPermissionGranted(*permissions)

            if (permissionsAlreadyRequested || permissionsGranted) {
                dismiss()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_dialog_title)
            .setView(R.layout.permissions_dialog_layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                settingsProvider.getMetaSettings().save(MetaKeys.PERMISSIONS_REQUESTED, true)

                permissionsProvider.requestPermissions(
                    requireActivity(),
                    object : PermissionListener {
                        override fun granted() {}
                    },
                    *permissions
                )
            }
            .create()
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    }
}
