package org.odk.collect.android.entities

import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.InMemEntitiesRepository

class InMemEntitiesRepositoryTest : EntitiesRepositoryTest() {

    override fun buildSubject(): EntitiesRepository {
        return InMemEntitiesRepository()
    }
}
