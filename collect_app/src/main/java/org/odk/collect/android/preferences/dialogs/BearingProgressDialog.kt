package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class BearingProgressDialog(
    private val buttonsListener: DialogInterface.OnClickListener
) : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isCancelable = false
        icon = android.R.drawable.ic_dialog_info
        title = getString(R.string.getting_bearing)
        message = getString(R.string.please_wait_long)
        setPositiveButton(getString(R.string.accept_bearing), buttonsListener)
        setNegativeButton(getString(R.string.cancel_location), buttonsListener)
    }
}
