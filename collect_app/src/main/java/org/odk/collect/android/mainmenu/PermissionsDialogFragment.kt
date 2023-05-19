package org.odk.collect.android.mainmenu

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider

class PermissionsDialogFragment(
    private val permissionsProvider: PermissionsProvider,
    private val requestPermissionsViewModel: RequestPermissionsViewModel
) : DialogFragment() {

    override fun onResume() {
        super.onResume()

        if (!requestPermissionsViewModel.shouldAskForPermissions()) {
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_dialog_title)
            .setView(R.layout.permissions_dialog_layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                requestPermissionsViewModel.permissionsRequested()

                permissionsProvider.requestPermissions(
                    requireActivity(),
                    object : PermissionListener {
                        override fun granted() {}
                    },
                    *requestPermissionsViewModel.permissions
                )
            }
            .create()
    }
}
