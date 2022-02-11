package org.odk.collect.android.formentry.saving

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragmentNew

class SaveAnswerFileProgressDialogFragment : MaterialProgressDialogFragmentNew() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        message = getString(R.string.saving_file)
    }
}
