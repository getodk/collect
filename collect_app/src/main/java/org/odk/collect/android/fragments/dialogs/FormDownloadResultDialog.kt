package org.odk.collect.android.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.ServerFormDetails

class FormDownloadResultDialog : DialogFragment() {
    companion object {
        const val RESULT_KEY = "RESULT"
    }

    private lateinit var result: HashMap<ServerFormDetails, String>
    private lateinit var listener: FormDownloadResultDialogListener

    interface FormDownloadResultDialogListener {
        fun onFormDownloadResultDialogOkButtonClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FormDownloadResultDialogListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        result = arguments?.getSerializable(RESULT_KEY) as HashMap<ServerFormDetails, String>

        val builder = AlertDialog.Builder(requireContext())
            .setMessage(getMessage())
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                listener.onFormDownloadResultDialogOkButtonClicked()
            }

        if (countFailures() > 0) {
            builder.setNegativeButton(getString(R.string.show_details)) { _, _ -> }
        }

        return builder.create()
    }

    private fun getMessage(): String {
        val numberOfFailures = countFailures()
        return if (numberOfFailures == 0) {
            getString(R.string.all_downloads_succeeded)
        } else {
            getString(R.string.some_downloads_failed, numberOfFailures.toString(), result.size.toString())
        }
    }

    private fun countFailures() =
        result.values.count { it != getString(R.string.success) }
}
