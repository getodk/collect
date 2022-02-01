package org.odk.collect.android.preferences.dialogs

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class FormDownloadListProgressDialog : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.canceling)
        message = getString(R.string.please_wait)
        icon = android.R.drawable.ic_dialog_info
        isCancelable = false
    }
}
