package org.odk.collect.android.support.pages

import org.odk.collect.testshared.AssertionFramework

class EntityListPage(private val list: String) : Page<EntityListPage>() {

    override fun assertOnPage(): EntityListPage {
        assertToolbarTitle(list)
        return this
    }

    fun assertEntity(label: String, properties: String): EntityListPage {
        assertText(label, assertionFramework = AssertionFramework.COMPOSE)
        assertText(properties, assertionFramework = AssertionFramework.COMPOSE)
        return this
    }
}
