package org.odk.collect.material

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Reusable [Fragment] based implementation of [https://www.material.io/components/dialogs#alert-dialog]
 */
class MaterialAlertDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(requireArguments().getString(ARG_MESSAGE))
            .setPositiveButton(getString(R.string.ok), null)
            .create()
    }

    companion object {
        const val ARG_MESSAGE = "arg_message"
    }
}
