package org.odk.collect.android.support.pages

import android.os.Build

class SaveOrDiscardFormDialog<D : Page<D>> @JvmOverloads constructor(
    private val destination: D,
    private val saveAsDraftEnabled: Boolean = true
) : Page<SaveOrDiscardFormDialog<D>>() {

    override fun assertOnPage(): SaveOrDiscardFormDialog<D> {
        if (saveAsDraftEnabled) {
            assertTextInDialog(org.odk.collect.strings.R.string.quit_form_title)
        } else {
            assertTextInDialog(org.odk.collect.strings.R.string.quit_form_continue_title)
        }

        return this
    }

    fun clickSaveChanges(): D {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.save_as_draft, destination)
    }

    fun clickSaveChangesWithError(errorMsg: Int): D {
        clickOnTextInDialog(org.odk.collect.strings.R.string.save_as_draft)
        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(errorMsg)
        } else {
            assertTextInDialog(errorMsg)
            clickOKOnDialog()
        }

        return destination.assertOnPage()
    }

    fun clickDiscardForm(): D {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.do_not_save, destination)
    }

    fun clickDiscardChanges(): D {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.discard_changes, destination)
    }
}
