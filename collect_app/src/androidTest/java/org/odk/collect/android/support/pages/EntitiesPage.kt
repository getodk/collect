package org.odk.collect.android.support.pages

class EntitiesPage : Page<EntitiesPage>() {

    override fun assertOnPage(): EntitiesPage {
        assertToolbarTitle(org.odk.collect.strings.R.string.entities_title)
        return this
    }

    fun clickOnList(datasetName: String): DatasetPage {
        clickOnText(datasetName)
        return DatasetPage(datasetName)
    }
}
