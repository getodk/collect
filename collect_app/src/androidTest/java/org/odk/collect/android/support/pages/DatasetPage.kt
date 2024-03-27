package org.odk.collect.android.support.pages

class DatasetPage(private val datasetName: String) : Page<DatasetPage>() {

    override fun assertOnPage(): DatasetPage {
        assertToolbarTitle(datasetName)
        return this
    }

    fun assertEntity(label: String, properties: String): DatasetPage {
        assertText(label)
        assertText(properties)
        return this
    }
}
