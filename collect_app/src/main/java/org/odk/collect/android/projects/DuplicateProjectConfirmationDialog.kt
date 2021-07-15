package org.odk.collect.android.projects

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.odk.collect.android.R

class DuplicateProjectConfirmationDialog : DialogFragment() {
    lateinit var listener: DialogInterface.OnClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = parentFragment as DialogInterface.OnClickListener

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.duplicate_project)
            .setMessage(R.string.duplicate_project_details)
            .setPositiveButton(R.string.add_duplicate_project, listener)
            .setNegativeButton(R.string.switch_to_existing, listener)
            .create()
    }
}
