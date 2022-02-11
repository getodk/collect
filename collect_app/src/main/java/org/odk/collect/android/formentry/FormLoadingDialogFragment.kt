/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.formentry

import android.content.Context
import org.odk.collect.material.MaterialProgressDialogFragmentNew.title
import org.odk.collect.material.MaterialProgressDialogFragmentNew.message
import org.odk.collect.material.MaterialProgressDialogFragmentNew.negativeButtonTitle
import org.odk.collect.material.MaterialProgressDialogFragmentNew.negativeButtonListener
import org.odk.collect.material.MaterialProgressDialogFragmentNew
import org.odk.collect.android.formentry.FormLoadingDialogFragment.FormLoadingDialogFragmentListener
import org.odk.collect.android.R
import android.content.DialogInterface

class FormLoadingDialogFragment : MaterialProgressDialogFragmentNew() {
    interface FormLoadingDialogFragmentListener {
        fun onCancelFormLoading()
    }

    /**
     * Using a listener like this requires an Activity to implement the interface. We could
     * use a similar approach as that used in [SaveFormProgressDialogFragment] and grab
     * a ViewModel to cancel.
     */
    @Deprecated("")
    private var listener: FormLoadingDialogFragmentListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.loading_form)
        message = getString(R.string.please_wait)
        isCancelable = false
        negativeButtonTitle = getString(R.string.cancel_loading_form)
        negativeButtonListener =
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int -> listener!!.onCancelFormLoading() }
        if (context is FormLoadingDialogFragmentListener) {
            listener = context
        }
    }
}