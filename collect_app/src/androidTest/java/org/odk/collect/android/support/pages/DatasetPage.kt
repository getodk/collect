package org.odk.collect.android.support.pages

class DatasetPage(private val datasetName: String) : Page<DatasetPage>() {

    override fun assertOnPage(): DatasetPage {
        assertToolbarTitle(datasetName)
        return this
    }

    fun assertEntity(fields: String): DatasetPage {
        assertText(fields)
        return this
    }
}
