package org.odk.collect.android.support.pages

class EntityListPage(private val list: String) : Page<EntityListPage>() {

    override fun assertOnPage(): EntityListPage {
        assertToolbarTitle(list)
        return this
    }

    fun assertEntity(label: String, properties: String): EntityListPage {
        assertText(label)
        assertText(properties)
        return this
    }
}
