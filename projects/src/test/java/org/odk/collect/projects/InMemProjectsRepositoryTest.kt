package org.odk.collect.projects

import org.odk.collect.shared.UUIDGenerator

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
