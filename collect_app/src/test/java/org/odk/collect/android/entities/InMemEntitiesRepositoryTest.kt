package org.odk.collect.android.entities

import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.InMemEntitiesRepository

class InMemEntitiesRepositoryTest : EntitiesRepositoryTest() {

    override fun buildSubject(): EntitiesRepository {
        return InMemEntitiesRepository()
    }
}
