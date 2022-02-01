package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class GoogleDriveProgressDialog(
    private val buttonListener: DialogInterface.OnClickListener,
    private val alertMsg: String
) : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.downloading_data)
        message = alertMsg
        isCancelable = false
        setNegativeButton(getString(R.string.cancel), buttonListener)
    }
}
