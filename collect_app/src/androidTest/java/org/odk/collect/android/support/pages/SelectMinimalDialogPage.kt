package org.odk.collect.android.support.pages

class SelectMinimalDialogPage(private val formName: String) : Page<SelectMinimalDialogPage>() {
    override fun assertOnPage(): SelectMinimalDialogPage {
        assertTextDoesNotExist(formName)
        return this
    }

    fun selectItem(item: String): FormEntryPage {
        return clickOnTextInDialog(item, FormEntryPage(formName))
    }
}
