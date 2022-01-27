package org.odk.collect.android.preferences.dialogs

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.strings.localization.getLocalizedString

class ResetProgressDialog : MaterialProgressDialogFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        setTitle(context.getLocalizedString(R.string.please_wait))
        setMessage(context.getLocalizedString(R.string.reset_in_progress))
        isCancelable = false
    }
}
