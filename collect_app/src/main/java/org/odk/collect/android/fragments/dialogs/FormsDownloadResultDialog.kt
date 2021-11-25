package org.odk.collect.android.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.errors.ErrorActivity
import org.odk.collect.errors.ErrorItem
import java.util.ArrayList

class FormsDownloadResultDialog : DialogFragment() {
    private lateinit var failures: ArrayList<ErrorItem>
    private var numberOfAllForms = 0

    var listener: FormDownloadResultDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FormDownloadResultDialogListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        failures = arguments?.getSerializable(ARG_FAILURES) as ArrayList<ErrorItem>
        numberOfAllForms = arguments?.getInt(ARG_NUMBER_OF_ALL_FORMS)!!

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(getMessage())
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                listener?.onCloseDownloadingResult()
            }

        if (failures.isNotEmpty()) {
            builder.setNegativeButton(getString(R.string.show_details)) { _, _ ->
                val intent = Intent(context, ErrorActivity::class.java).apply {
                    putExtra(ErrorActivity.EXTRA_ERRORS, failures)
                }
                startActivity(intent)
                listener?.onCloseDownloadingResult()
            }
        }

        return builder.create()
    }

    private fun getMessage(): String {
        return if (failures.isEmpty()) {
            getString(R.string.all_downloads_succeeded)
        } else {
            getString(R.string.some_downloads_failed, failures.size.toString(), numberOfAllForms.toString())
        }
    }

    interface FormDownloadResultDialogListener {
        fun onCloseDownloadingResult()
    }

    companion object {
        const val ARG_FAILURES = "FAILURES"
        const val ARG_NUMBER_OF_ALL_FORMS = "NUMBER_OF_ALL_FORMS"
    }
}
