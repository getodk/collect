package org.odk.collect.android.support.rules

import org.odk.collect.android.support.pages.FormEntryPage

class BlankFormTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : FormEntryActivityTestRule() {

    private lateinit var formEntryPage: FormEntryPage

    override fun before() {
        super.before()
        setUpProjectAndCopyForm(formFilename, mediaFilePaths)
        formEntryPage = fillNewForm(formFilename, formName)
    }

    fun fillNewForm(): FormEntryPage {
        return formEntryPage
    }
}
