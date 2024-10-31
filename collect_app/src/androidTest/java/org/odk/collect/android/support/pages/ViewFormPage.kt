package org.odk.collect.android.support.pages

class ViewFormPage(private val formName: String) : Page<ViewFormPage>() {

    override fun assertOnPage(): ViewFormPage {
        assertToolbarTitle(formName)
        assertText(org.odk.collect.strings.R.string.exit)
        return this
    }
}
