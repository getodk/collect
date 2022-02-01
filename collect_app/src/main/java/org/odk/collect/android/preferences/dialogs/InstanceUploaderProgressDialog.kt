package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class InstanceUploaderProgressDialog(
    private val alertMsg: String,
    private val onButtonClick: DialogInterface.OnClickListener
) : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.uploading_data)
        message = alertMsg
        isCancelable = false
        setNegativeButton(getString(R.string.cancel), onButtonClick)
    }
}
