package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class SaveOrIgnoreDrawingDialog<D : Page<D>>(
    private val drawingName: String,
    private val destination: D
) : Page<SaveOrIgnoreDrawingDialog<D>>() {

    override fun assertOnPage(): SaveOrIgnoreDrawingDialog<D> {
        val title = getTranslatedString(R.string.exit) + " " + drawingName
        assertText(title)
        return this
    }

    fun clickSaveChanges(): D {
        clickOnString(R.string.keep_changes)
        return destination.assertOnPage()
    }

    fun clickDiscardChanges(): D {
        clickOnString(R.string.discard_changes)
        return destination.assertOnPage()
    }
}
