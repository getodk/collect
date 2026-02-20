package org.odk.collect.experimental.timedgrid

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.experimental.R

class OngoingAssessmentWarningDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.assessment)
            .setMessage(R.string.assessment_warning)
            .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
            .create()
    }
}
