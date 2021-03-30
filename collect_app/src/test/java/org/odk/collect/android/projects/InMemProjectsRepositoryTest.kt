package org.odk.collect.android.projects

import org.odk.collect.android.utilities.UUIDGenerator

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
