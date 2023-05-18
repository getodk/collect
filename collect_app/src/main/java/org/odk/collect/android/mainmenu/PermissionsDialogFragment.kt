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
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys

class PermissionsDialogFragment(
    private val settingsProvider: SettingsProvider,
    private val permissionsProvider: PermissionsProvider
) : DialogFragment() {

    override fun onResume() {
        super.onResume()

        val oldAPI = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        val permissionsAlreadyRequested =
            settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED)

        if (oldAPI || permissionsAlreadyRequested) {
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About permissions")
            .setView(R.layout.permissions_dialog_layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                settingsProvider.getMetaSettings().save(MetaKeys.PERMISSIONS_REQUESTED, true)

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
