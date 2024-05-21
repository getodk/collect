package org.odk.collect.android.support.pages

class EntitiesPage : Page<EntitiesPage>() {

    override fun assertOnPage(): EntitiesPage {
        assertToolbarTitle(org.odk.collect.strings.R.string.entities_title)
        return this
    }

    fun clickOnList(list: String): EntityListPage {
        clickOnText(list)
        return EntityListPage(list)
    }
}
