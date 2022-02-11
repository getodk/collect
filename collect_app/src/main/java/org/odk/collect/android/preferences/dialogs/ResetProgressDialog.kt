package org.odk.collect.android.preferences.dialogs

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragmentNew
import org.odk.collect.strings.localization.getLocalizedString

class ResetProgressDialog : MaterialProgressDialogFragmentNew() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        title = context.getLocalizedString(R.string.please_wait)
        message = context.getLocalizedString(R.string.reset_in_progress)
        canBeCanceled = false
    }
}
