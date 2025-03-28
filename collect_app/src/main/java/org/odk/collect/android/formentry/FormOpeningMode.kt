package org.odk.collect.android.formentry

object FormOpeningMode {
    const val FORM_MODE_KEY = "formMode"

    const val EDIT_SAVED = "editSaved"
    const val EDIT_FINALIZED = "editFinalized"
    const val VIEW_SENT = "viewSent"

    @JvmStatic
    fun isEditableMode(mode: String?): Boolean {
        return mode == null ||
            mode.equals(EDIT_SAVED, true) ||
            mode.equals(EDIT_FINALIZED, true)
    }
}
