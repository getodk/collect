package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class EntitiesPage : Page<EntitiesPage>() {

    override fun assertOnPage(): EntitiesPage {
        assertToolbarTitle(org.odk.collect.strings.R.string.entities_title)
        return this
    }

    fun clickOnDataset(datasetName: String): DatasetPage {
        clickOnText(datasetName)
        return DatasetPage(datasetName)
    }
}
