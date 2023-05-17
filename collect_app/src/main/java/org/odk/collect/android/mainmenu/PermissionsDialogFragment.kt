package org.odk.collect.android.mainmenu

import android.Manifest
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider

class PermissionsDialogFragment(
    private val permissionChecker: PermissionsChecker,
    private val permissionsProvider: PermissionsProvider
) : DialogFragment() {

    override fun onResume() {
        super.onResume()

        val shouldAskForPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionChecker.shouldAskForPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }

        if (!shouldAskForPermission) {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permissions update")
            .setView(R.layout.permissions_dialog_layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                permissionsProvider.requestPermissions(
                    requireActivity(),
                    object : PermissionListener {
                        override fun granted() {}
                    },
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            .create()
    }
}
