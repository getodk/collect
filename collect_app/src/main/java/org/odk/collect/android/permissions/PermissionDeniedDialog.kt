package org.odk.collect.android.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R

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
            .create()
    }

    companion object {
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val ICON = "icon"
    }
}
