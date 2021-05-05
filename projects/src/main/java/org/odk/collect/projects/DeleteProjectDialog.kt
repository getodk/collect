package org.odk.collect.projects

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteProjectDialog : DialogFragment() {

    private var listener: DeleteProjectListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is DeleteProjectListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete_project_confirm_message)
            .setNegativeButton(R.string.delete_project_no) { _: DialogInterface?, _: Int ->
                dismiss()
            }
            .setPositiveButton(R.string.delete_project_yes) { _: DialogInterface?, _: Int ->
                listener?.deleteProject()
            }
            .create()
    }

    interface DeleteProjectListener {
        fun deleteProject()
    }
}
