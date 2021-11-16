package org.odk.collect.permissions

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionDeniedDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = requireArguments().getInt(TITLE)
        val message = requireArguments().getInt(MESSAGE)
        val icon = requireArguments().getInt(ICON)

        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(icon)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setNeutralButton(R.string.open_settings) { _, _ ->
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                    startActivity(this)
                }
            }
            .create()
    }

    companion object {
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val ICON = "icon"
    }
}
