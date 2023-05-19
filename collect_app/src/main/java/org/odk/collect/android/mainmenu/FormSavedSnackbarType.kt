package org.odk.collect.android.mainmenu

import org.odk.collect.android.R

enum class FormSavedSnackbarType(
    val message: Int,
    val actionName: Int
) {
    SAVED_AS_DRAFT(R.string.form_saved_as_draft, R.string.edit_form),
    FINALIZED(R.string.form_saved, R.string.view_form),
    SENDING(R.string.form_sending, R.string.view_form),
    SENDING_NO_INTERNET_CONNECTION(R.string.form_sending_failed, R.string.view_form)
}
