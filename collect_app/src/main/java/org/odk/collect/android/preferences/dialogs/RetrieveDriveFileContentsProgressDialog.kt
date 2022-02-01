package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragment

class RetrieveDriveFileContentsProgressDialog(
    private val buttonListener: DialogInterface.OnClickListener
) : MaterialProgressDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        message = getString(R.string.reading_files)
        isCancelable = false
        setNegativeButton(getString(R.string.cancel), buttonListener)
    }
}
