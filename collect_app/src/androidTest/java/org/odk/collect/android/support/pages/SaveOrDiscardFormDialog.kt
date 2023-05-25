package org.odk.collect.android.support.pages

import android.os.Build
import org.odk.collect.android.R

class SaveOrDiscardFormDialog<D : Page<D>>(
    private val destination: D
) : Page<SaveOrDiscardFormDialog<D>>() {

    override fun assertOnPage(): SaveOrDiscardFormDialog<D> {
        assertText(R.string.quit_form_title)
        return this
    }

    fun clickSaveChanges(): D {
        clickOnString(R.string.save_as_draft)
        return destination.assertOnPage()
    }

    fun clickSaveChangesWithError(errorMsg: Int): D {
        clickOnString(R.string.save_as_draft)
        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(errorMsg)
        } else {
            assertText(errorMsg)
            clickOKOnDialog()
        }
        return destination.assertOnPage()
    }

    fun clickDiscardForm(): D {
        clickOnString(R.string.do_not_save)
        return destination.assertOnPage()
    }

    fun clickDiscardChanges(): D {
        clickOnString(R.string.discard_changes)
        return destination.assertOnPage()
    }
}
