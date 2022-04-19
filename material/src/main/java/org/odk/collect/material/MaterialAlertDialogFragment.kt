package org.odk.collect.material

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialAlertDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(requireArguments().getString(ARG_MESSAGE))
            .create()
    }

    companion object {
        const val ARG_MESSAGE = "arg_message"
    }
}
