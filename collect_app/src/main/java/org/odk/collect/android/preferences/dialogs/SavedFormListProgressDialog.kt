package org.odk.collect.android.preferences.dialogs

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class SavedFormListProgressDialog : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        message = getString(R.string.form_delete_message)
        isCancelable = false
    }
}
