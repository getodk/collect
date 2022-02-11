package org.odk.collect.android.formentry

import android.content.Context
import android.content.DialogInterface
import org.odk.collect.android.R
import org.odk.collect.material.MaterialProgressDialogFragmentNew

class RefreshFormListDialogFragment : MaterialProgressDialogFragmentNew() {
    lateinit var listener: RefreshFormListDialogFragmentListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RefreshFormListDialogFragmentListener) {
            listener = context
        }
        title = getString(R.string.downloading_data)
        message = getString(R.string.please_wait)
        canBeCanceled = false
        negativeButtonTitle = getString(R.string.cancel_loading_form)
        negativeButtonListener =
            DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                listener.onCancelFormLoading()
                dismiss()
            }
    }

    interface RefreshFormListDialogFragmentListener {
        fun onCancelFormLoading()
    }
}
