package org.odk.collect.android.preferences.dialogs

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment
import org.odk.collect.strings.localization.getLocalizedString

class ResetProgressDialog : ProgressDialogFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        setTitle(context.getLocalizedString(R.string.please_wait))
        setMessage(context.getLocalizedString(R.string.reset_in_progress))
        isCancelable = false
    }
}
