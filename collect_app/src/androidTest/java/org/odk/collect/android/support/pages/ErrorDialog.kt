package org.odk.collect.android.support.pages

import org.odk.collect.strings.R

class ErrorDialog : OkDialog() {
    fun assertOnPage(isFatal: Boolean): ErrorDialog {
        assertOnPage()
        if (isFatal) {
            assertText(R.string.form_cannot_be_used)
        } else {
            assertText(R.string.error_occured)
        }
        return this
    }
}
