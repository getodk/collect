package org.odk.collect.android.formlists.savedformlist

import org.odk.collect.android.R
import org.odk.collect.forms.instances.Instance

object SavedFormUtils {

    @JvmStatic
    fun getIcon(instance: Instance): Int {
        return getIcon(instance.status)
    }

    @JvmStatic
    fun getIcon(status: String): Int {
        return when (status) {
            Instance.STATUS_INCOMPLETE, Instance.STATUS_INVALID, Instance.STATUS_VALID -> R.drawable.ic_form_state_saved
            Instance.STATUS_COMPLETE -> R.drawable.ic_form_state_finalized
            Instance.STATUS_SUBMITTED -> R.drawable.ic_form_state_submitted
            Instance.STATUS_SUBMISSION_FAILED -> R.drawable.ic_form_state_submission_failed
            else -> throw IllegalArgumentException()
        }
    }
}
