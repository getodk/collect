package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class ViewFormPage(private val formName: String) : Page<ViewFormPage>() {

    override fun assertOnPage(): ViewFormPage {
        assertToolbarTitle(formName)
        assertText(R.string.exit)
        return this
    }
}
