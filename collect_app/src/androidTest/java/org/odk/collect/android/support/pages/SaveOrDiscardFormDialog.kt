package org.odk.collect.android.support.pages

import android.os.Build
import org.odk.collect.android.R

class SaveOrDiscardFormDialog<D : Page<D>> @JvmOverloads constructor(
    private val destination: D,
    private val saveAsDraftEnabled: Boolean = true
) : Page<SaveOrDiscardFormDialog<D>>() {

    override fun assertOnPage(): SaveOrDiscardFormDialog<D> {
        if (saveAsDraftEnabled) {
            assertText(org.odk.collect.strings.R.string.quit_form_title)
        } else {
            assertText(org.odk.collect.strings.R.string.quit_form_continue_title)
        }

        return this
    }

    fun clickSaveChanges(): D {
        clickOnString(org.odk.collect.strings.R.string.save_as_draft)
        return destination.assertOnPage()
    }

    fun clickSaveChangesWithError(errorMsg: Int): D {
        clickOnString(org.odk.collect.strings.R.string.save_as_draft)
        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(errorMsg)
        } else {
            assertText(errorMsg)
            clickOKOnDialog()
        }
        return destination.assertOnPage()
    }

    fun clickDiscardForm(): D {
        clickOnString(org.odk.collect.strings.R.string.do_not_save)
        return destination.assertOnPage()
    }

    fun clickDiscardChanges(): D {
        clickOnString(org.odk.collect.strings.R.string.discard_changes)
        return destination.assertOnPage()
    }
}
